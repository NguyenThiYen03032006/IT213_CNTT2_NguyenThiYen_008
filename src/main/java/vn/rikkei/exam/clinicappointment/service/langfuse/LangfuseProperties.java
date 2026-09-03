package vn.rikkei.exam.clinicappointment.service.langfuse;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "langfuse")
public class LangfuseProperties {

    private String publicKey = "";
    private String secretKey = "";
    private String host = "http://localhost:3000";

    public boolean isConfigured() {
        return publicKey != null && !publicKey.isBlank()
                && secretKey != null && !secretKey.isBlank()
                && host != null && !host.isBlank();
    }
}
