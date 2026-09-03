package vn.rikkei.exam.clinicappointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String answer;
    private String conversationId;
    private List<String> sources;
    private List<String> toolsUsed;
}