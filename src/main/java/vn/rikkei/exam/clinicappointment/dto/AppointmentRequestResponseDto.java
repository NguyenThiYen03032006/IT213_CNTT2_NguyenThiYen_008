package vn.rikkei.exam.clinicappointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestResponseDto {
    private String requestId;
    private String summary;
}
