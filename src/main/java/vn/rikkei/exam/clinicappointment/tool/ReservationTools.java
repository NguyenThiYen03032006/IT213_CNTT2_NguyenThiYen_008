package vn.rikkei.exam.clinicappointment.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import vn.rikkei.exam.clinicappointment.dto.AppointmentAvailabilityDto;
import vn.rikkei.exam.clinicappointment.dto.AppointmentRequestResponseDto;
import vn.rikkei.exam.clinicappointment.service.ReservationService;
import vn.rikkei.exam.clinicappointment.service.chat.ToolExecutionTracker;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTools {

    private final ReservationService reservationService;

    @Tool(description = "Tra cuu tinh trang lich kham con kha dung theo khoang ngay. Bat buoc: startDate phai nho hon endDate.")
    public String getAppointmentAvailability(
            @ToolParam(description = "Ma loai lich kham (vi du: STD cho Standard, PRM cho Premium)") String resourceType,
            @ToolParam(description = "Ngay bat dau, dinh dang yyyy-MM-dd") String startDate,
            @ToolParam(description = "Ngay ket thuc, dinh dang yyyy-MM-dd") String endDate) {

        ToolExecutionTracker.record("getAppointmentAvailability");
        log.info("Tool getAppointmentAvailability duoc goi: resourceType={}, startDate={}, endDate={}",
                resourceType, startDate, endDate);

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate.trim());
            end = LocalDate.parse(endDate.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            return "Loi validation: Dinh dang ngay khong hop le. Vui long su dung dinh dang yyyy-MM-dd.";
        }

        if (!start.isBefore(end)) {
            return "Loi validation: Ngay bat dau (startDate) phai nho hon ngay ket thuc (endDate).";
        }

        try {
            List<AppointmentAvailabilityDto> availability = reservationService.getAppointmentAvailability(resourceType, start, end);
            if (availability.isEmpty()) {
                return String.format("Khong co lich kham kha dung nao cho loai %s tu ngay %s den ngay %s.",
                        resourceType, start, end);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Lich kham kha dung cho loai %s tu %s den %s:\n", resourceType, start, end));
            for (AppointmentAvailabilityDto dto : availability) {
                sb.append(String.format("- Ngay %s: Con %d slot trong (Suc chua toi da: %d nguoi/yeu cau)\n",
                        dto.getDate(), dto.getAvailableSlots(), dto.getMaxParticipants()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Loi tra cuu: " + e.getMessage();
        }
    }

    @Tool(description = "Tao yeu cau dat lich kham moi voi trang thai PENDING. Kiem tra user, thoi han toi da 14 ngay, suc chua va muc dich kham.")
    public String createAppointmentRequest(
            @ToolParam(description = "Ma nguoi dung (vi du: USR-001, USR-002)") String userId,
            @ToolParam(description = "Ma loai lich kham (vi du: STD, PRM)") String resourceType,
            @ToolParam(description = "Ngay bat dau, dinh dang yyyy-MM-dd") String startDate,
            @ToolParam(description = "Ngay ket thuc, dinh dang yyyy-MM-dd") String endDate,
            @ToolParam(description = "So luong nguoi tham gia kham") int participantCount,
            @ToolParam(description = "Muc dich kham benh (vi du: Kham suc khoe tong quat)") String purpose) {

        ToolExecutionTracker.record("createAppointmentRequest");
        log.info("Tool createAppointmentRequest duoc goi: userId={}, resourceType={}, startDate={}, endDate={}, participants={}",
                userId, resourceType, startDate, endDate, participantCount);

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate.trim());
            end = LocalDate.parse(endDate.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            return "Loi validation: Dinh dang ngay khong hop le. Vui long su dung dinh dang yyyy-MM-dd.";
        }

        try {
            AppointmentRequestResponseDto response = reservationService.createAppointmentRequest(
                    userId, resourceType, start, end, participantCount, purpose);

            return String.format("Tao yeu cau dat lich thanh cong! Ma yeu cau (requestId): %s. Chi tiet: %s",
                    response.getRequestId(), response.getSummary());
        } catch (Exception e) {
            log.warn("Tao yeu cau dat lich that bai do vi pham business rule: {}", e.getMessage());
            return "Loi tao yeu cau dat lich: " + e.getMessage();
        }
    }
}
