package vn.rikkei.exam.clinicappointment.service.langfuse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class LangfuseService {

    private final LangfuseProperties properties;

    public void logTrace(
            String traceId,
            String name,
            String input,
            String output,
            long durationMs,
            String conversationId,
            List<String> toolsUsed,
            List<String> sources) {

        List<String> safeTools = toolsUsed != null ? toolsUsed : Collections.emptyList();
        List<String> safeSources = sources != null ? sources : Collections.emptyList();

        log.info("[Langfuse Trace] traceId: {}, name: {}, latency: {}ms, conversationId: {}, examCode: DE-008, toolsUsed: {}, sources: {} | Input: {} | Output: {}",
                traceId, name, durationMs, conversationId, safeTools, safeSources,
                input != null && input.length() > 80 ? input.substring(0, 80) + "..." : input,
                output != null && output.length() > 80 ? output.substring(0, 80) + "..." : output);

        if (properties.isConfigured()) {
            try {
                RestClient restClient = RestClient.builder()
                        .baseUrl(properties.getHost())
                        .build();

                String credentials = properties.getPublicKey() + ":" + properties.getSecretKey();
                String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("conversationId", conversationId);
                metadata.put("examCode", "DE-008");
                metadata.put("toolsUsed", safeTools);
                metadata.put("sources", safeSources);

                Map<String, Object> traceBody = new LinkedHashMap<>();
                traceBody.put("id", traceId);
                traceBody.put("name", name);
                traceBody.put("sessionId", conversationId);
                traceBody.put("input", input);
                traceBody.put("output", output);
                traceBody.put("metadata", metadata);

                Map<String, Object> batchEvent = new LinkedHashMap<>();
                batchEvent.put("id", UUID.randomUUID().toString());
                batchEvent.put("type", "trace-create");
                batchEvent.put("timestamp", Instant.now().toString());
                batchEvent.put("body", traceBody);

                Map<String, Object> payload = Map.of("batch", List.of(batchEvent));

                restClient.post()
                        .uri("/api/public/ingestion")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();

                log.debug("Da gui trace {} len Langfuse thanh cong", traceId);
            } catch (Exception e) {
                log.warn("Khong the gui trace len Langfuse Server: {}", e.getMessage());
            }
        }
    }

    public Map<String, Object> checkHealth() {
        if (!properties.isConfigured()) {
            return Map.of(
                    "status", "NOT_CONFIGURED",
                    "host", properties.getHost(),
                    "message", "Langfuse chua duoc cau hinh (thieu public-key hoac secret-key)");
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(properties.getHost())
                    .build();

            String credentials = properties.getPublicKey() + ":" + properties.getSecretKey();
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            String fromStartTime = Instant.now().minus(1, ChronoUnit.DAYS).toString();
            String toStartTime = Instant.now().toString();

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/public/v2/observations")
                            .queryParam("fromStartTime", fromStartTime)
                            .queryParam("toStartTime", toStartTime)
                            .queryParam("limit", 1)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            return Map.of(
                    "status", "CONNECTED",
                    "host", properties.getHost(),
                    "response", response != null ? "OK" : "EMPTY");
        } catch (Exception e) {
            log.warn("Loi ket noi Langfuse: {}", e.getMessage());
            return Map.of(
                    "status", "CONNECTION_FAILED",
                    "host", properties.getHost(),
                    "error", e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
