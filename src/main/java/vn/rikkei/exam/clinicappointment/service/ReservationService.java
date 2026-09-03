package vn.rikkei.exam.clinicappointment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.rikkei.exam.clinicappointment.dto.AppointmentAvailabilityDto;
import vn.rikkei.exam.clinicappointment.dto.AppointmentRequestResponseDto;
import vn.rikkei.exam.clinicappointment.dto.ApproveRequestDto;
import vn.rikkei.exam.clinicappointment.dto.ApproveResponseDto;
import vn.rikkei.exam.clinicappointment.exception.BusinessRuleException;
import vn.rikkei.exam.clinicappointment.exception.ResourceNotFoundException;
import vn.rikkei.exam.clinicappointment.model.AppUser;
import vn.rikkei.exam.clinicappointment.model.ReservationRequest;
import vn.rikkei.exam.clinicappointment.model.ReservationStatus;
import vn.rikkei.exam.clinicappointment.model.ResourceInventory;
import vn.rikkei.exam.clinicappointment.model.ResourceType;
import vn.rikkei.exam.clinicappointment.repository.AppUserRepository;
import vn.rikkei.exam.clinicappointment.repository.ReservationRequestRepository;
import vn.rikkei.exam.clinicappointment.repository.ResourceInventoryRepository;
import vn.rikkei.exam.clinicappointment.repository.ResourceTypeRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    public static final int MAX_APPOINTMENT_DAYS = 14;
    public static final int MIN_PURPOSE_LENGTH = 10;
    public static final int MAX_PURPOSE_LENGTH = 200;
    public static final int PREMIUM_MIN_PARTICIPANTS = 2;

    private final AppUserRepository appUserRepository;
    private final ReservationRequestRepository reservationRequestRepository;
    private final ResourceInventoryRepository resourceInventoryRepository;
    private final ResourceTypeRepository resourceTypeRepository;

    @Transactional(readOnly = true)
    public List<AppointmentAvailabilityDto> getAppointmentAvailability(
            String resourceType, LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngay bat dau va ngay ket thuc khong duoc de trong.");
        }

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate phai nho hon endDate.");
        }

        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Loai tai nguyen (resourceType) khong duoc de trong.");
        }

        String code = resourceType.trim().toUpperCase();
        ResourceType type = resourceTypeRepository.findByResourceCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay loai tai nguyen: " + resourceType));

        List<ResourceInventory> inventories = resourceInventoryRepository
                .findByResourceType_ResourceCodeAndAvailableDateBetween(type.getResourceCode(), startDate, endDate);

        List<AppointmentAvailabilityDto> result = new ArrayList<>();
        for (ResourceInventory inv : inventories) {
            result.add(AppointmentAvailabilityDto.builder()
                    .resourceCode(type.getResourceCode())
                    .resourceName(type.getDisplayName())
                    .date(inv.getAvailableDate())
                    .availableSlots(inv.getAvailableSlots())
                    .maxParticipants(type.getMaxParticipants())
                    .build());
        }

        return result;
    }

    @Transactional
    public AppointmentRequestResponseDto createAppointmentRequest(
            String userId,
            String resourceType,
            LocalDate startDate,
            LocalDate endDate,
            Integer participantCount,
            String purpose) {

        log.info("Bat dau tao yeu cau dat lich: userId={}, resourceType={}, startDate={}, endDate={}, participants={}",
                userId, resourceType, startDate, endDate, participantCount);

        if (userId == null || userId.isBlank()) {
            throw new BusinessRuleException("userId khong duoc de trong.");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new BusinessRuleException("resourceType khong duoc de trong.");
        }
        if (startDate == null || endDate == null) {
            throw new BusinessRuleException("startDate va endDate khong duoc de trong.");
        }
        if (participantCount == null || participantCount <= 0) {
            throw new BusinessRuleException("So nguoi tham gia (participantCount) phai lon hon 0.");
        }

        AppUser user = appUserRepository.findById(userId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Nguoi dung khong ton tai voi ma: " + userId));

        if (!startDate.isBefore(endDate)) {
            throw new BusinessRuleException("startDate phai nho hon endDate.");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days > MAX_APPOINTMENT_DAYS) {
            throw new BusinessRuleException("Thoi gian dat lich vuot qua quy dinh toi da 14 ngay (hien tai: " + days + " ngay).");
        }

        String code = resourceType.trim().toUpperCase();
        ResourceType type = resourceTypeRepository.findByResourceCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Loai tai nguyen khong ton tai: " + resourceType));

        if (participantCount > type.getMaxParticipants()) {
            throw new BusinessRuleException(String.format(
                    "So luong nguoi tham gia (%d) vuot qua suc chua toi da (%d) cua nhom %s.",
                    participantCount, type.getMaxParticipants(), type.getDisplayName()));
        }

        String validPurpose = (purpose == null || purpose.isBlank())
                ? "Kham suc khoe dinh ky"
                : purpose.trim();

        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Instant now = Instant.now();

        ReservationRequest request = ReservationRequest.builder()
                .requestId(requestId)
                .requester(user)
                .resourceType(type)
                .startDate(startDate)
                .endDate(endDate)
                .participantCount(participantCount)
                .purpose(validPurpose)
                .status(ReservationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        reservationRequestRepository.save(request);
        log.info("Tao yeu cau dat lich thanh cong: requestId={}", requestId);

        String summary = String.format(
                "Yeu cau dat lich %s ma %s cua nguoi dung %s (%s) tu %s den %s cho %d nguoi da duoc tao thanh cong voi trang thai PENDING.",
                type.getDisplayName(), requestId, user.getFullName(), user.getUserId(), startDate, endDate, participantCount);

        return AppointmentRequestResponseDto.builder()
                .requestId(requestId)
                .summary(summary)
                .build();
    }

    @Transactional
    public ApproveResponseDto processApprovalRequest(ApproveRequestDto dto) {
        if (dto == null || dto.getRequestId() == null || dto.getRequestId().isBlank()) {
            throw new BusinessRuleException("requestId khong duoc de trong.");
        }

        String decision = dto.getDecision() != null ? dto.getDecision().trim().toUpperCase() : "";
        if (!"APPROVE".equals(decision) && !"REJECT".equals(decision)) {
            throw new BusinessRuleException("decision chi chap nhan APPROVE hoac REJECT (hien tai: " + dto.getDecision() + ").");
        }

        ReservationRequest request = reservationRequestRepository.findById(dto.getRequestId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Yeu cau dat lich khong ton tai voi ma: " + dto.getRequestId()));

        if (request.getStatus() == ReservationStatus.APPROVED) {
            throw new BusinessRuleException("Khong the xu ly yeu cau da duoc APPROVE truoc do.");
        }
        if (request.getStatus() == ReservationStatus.REJECTED) {
            throw new BusinessRuleException("Khong the xu ly yeu cau da bi REJECT truoc do.");
        }
        if (request.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Chi xu ly yeu cau o trang thai PENDING. Trang thai hien tai: " + request.getStatus());
        }

        if ("APPROVE".equals(decision)) {
            if (request.getRequester() == null || !appUserRepository.existsById(request.getRequester().getUserId())) {
                throw new BusinessRuleException("Khong the approve: Nguoi dung khong con ton tai trong he thong.");
            }
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
            if (days > MAX_APPOINTMENT_DAYS || !request.getStartDate().isBefore(request.getEndDate())) {
                throw new BusinessRuleException("Khong the approve: Khoang thoi gian dat lich khong hop le hoac vuot qua 14 ngay.");
            }
            ResourceType type = request.getResourceType();
            if (type == null) {
                throw new BusinessRuleException("Khong the approve: Loai tai nguyen khong hop le.");
            }
            if (request.getParticipantCount() > type.getMaxParticipants()) {
                throw new BusinessRuleException("Khong the approve: So nguoi vuot qua suc chua toi da.");
            }

            request.setStatus(ReservationStatus.APPROVED);
        } else {
            request.setStatus(ReservationStatus.REJECTED);
        }

        request.setDecisionNote(dto.getNote() != null ? dto.getNote().trim() : null);
        request.setUpdatedAt(Instant.now());

        reservationRequestRepository.save(request);

        log.info("Da xu ly phe duyet request {}: decision={}, status={}",
                request.getRequestId(), decision, request.getStatus());

        return ApproveResponseDto.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .decisionNote(request.getDecisionNote())
                .updatedAt(request.getUpdatedAt())
                .message("Yeu cau " + request.getRequestId() + " da duoc " + request.getStatus().name())
                .build();
    }
}
