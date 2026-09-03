package vn.rikkei.exam.clinicappointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    @NotBlank
    private String message;

    private String conversationId;
}
