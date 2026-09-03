package vn.rikkei.exam.clinicappointment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.rikkei.exam.clinicappointment.dto.ChatRequest;
import vn.rikkei.exam.clinicappointment.dto.ChatResponse;
import vn.rikkei.exam.clinicappointment.service.chat.ChatAssistantService;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final ChatAssistantService chatAssistantService;

    @PostMapping({"/ask", "/chat"})
    public ChatResponse ask(@Valid @RequestBody ChatRequest request) {
        return chatAssistantService.chat(request.getMessage(), request.getConversationId());
    }
}