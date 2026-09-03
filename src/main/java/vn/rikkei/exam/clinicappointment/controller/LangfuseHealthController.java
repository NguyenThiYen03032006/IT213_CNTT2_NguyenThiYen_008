package vn.rikkei.exam.clinicappointment.controller;


import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.rikkei.exam.clinicappointment.service.langfuse.LangfuseService;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/langfuse", "/api/v1/langfuse"})
public class LangfuseHealthController {

    private final LangfuseService langfuseService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(langfuseService.checkHealth());
    }
}
