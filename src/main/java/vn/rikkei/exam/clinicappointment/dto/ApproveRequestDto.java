package vn.rikkei.exam.clinicappointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequestDto {

    @NotBlank(message = "requestId khong duoc de trong")
    private String requestId;

    @NotBlank(message = "decision khong duoc de trong")
    private String decision;

    private String note;
}
