package vn.rikkei.exam.clinicappointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.rikkei.exam.clinicappointment.model.ReservationStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveResponseDto {
    private String requestId;
    private ReservationStatus status;
    private String decisionNote;
    private Instant updatedAt;
    private String message;
}
