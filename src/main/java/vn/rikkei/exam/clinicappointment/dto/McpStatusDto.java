package vn.rikkei.exam.clinicappointment.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpStatusDto {

    private boolean enabled;
    private boolean connected;
    private String endpoint;
    private String clientType;
    private int toolCount;
    private List<String> availableTools;
    private String message;
}
