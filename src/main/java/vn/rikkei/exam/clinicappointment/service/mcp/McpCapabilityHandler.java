package vn.rikkei.exam.clinicappointment.service.mcp;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class McpCapabilityHandler {

    private boolean supportsTools;
    private boolean supportsResources;
    private boolean supportsPrompts;
    private boolean supportsLogging;
    private String serverName;
    private String serverVersion;
    private List<String> toolNames;

    public static McpCapabilityHandler disabled() {
        return McpCapabilityHandler.builder()
                .supportsTools(false)
                .supportsResources(false)
                .supportsPrompts(false)
                .supportsLogging(false)
                .serverName("None (Local Fallback)")
                .serverVersion("N/A")
                .toolNames(Collections.emptyList())
                .build();
    }
}
