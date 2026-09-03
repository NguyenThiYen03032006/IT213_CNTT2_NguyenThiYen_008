package vn.rikkei.exam.clinicappointment.service.mcp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mcp.antigravity")
public class AntigravityMcpProperties {

    private boolean enabled = false;

    private String transport = "sse";

    private String endpoint = "";

    private String token = "";

    private int timeoutSeconds = 30;
}
