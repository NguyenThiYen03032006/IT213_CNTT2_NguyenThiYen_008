package vn.rikkei.exam.clinicappointment.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.rikkei.exam.clinicappointment.dto.McpStatusDto;
import vn.rikkei.exam.clinicappointment.service.mcp.AntigravityMcpService;
import vn.rikkei.exam.clinicappointment.service.mcp.McpCapabilityHandler;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final AntigravityMcpService mcpService;

    @GetMapping("/status")
    public ResponseEntity<McpStatusDto> getStatus() {
        return ResponseEntity.ok(mcpService.getStatus());
    }

    @GetMapping("/capabilities")
    public ResponseEntity<McpCapabilityHandler> getCapabilities() {
        return ResponseEntity.ok(mcpService.getCapabilities());
    }
}
