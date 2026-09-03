package vn.rikkei.exam.clinicappointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        normalizeOpenAiBaseUrl();
        SpringApplication.run(Application.class, args);
    }

    private static void normalizeOpenAiBaseUrl() {
        String baseUrl = System.getenv("OPENAI_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = System.getenv("OPEN_ROUTER_BASED_URL");
        }
        if (baseUrl != null && !baseUrl.isBlank()) {
            String trimmed = baseUrl.trim();
            if (trimmed.endsWith("/api")) {
                trimmed = trimmed + "/v1";
            } else if (!trimmed.endsWith("/v1") && !trimmed.endsWith("/")) {
                trimmed = trimmed + "/api/v1";
            }
            System.setProperty("spring.ai.openai.base-url", trimmed);
        }
    }
}
