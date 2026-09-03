package vn.rikkei.exam.clinicappointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAvailabilityDto {
    private String resourceCode;
    private String resourceName;
    private LocalDate date;
    private Integer availableSlots;
    private Integer maxParticipants;
}
