package vn.rikkei.exam.clinicappointment.service.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import vn.rikkei.exam.clinicappointment.dto.McpStatusDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class AntigravityMcpService {

    private final AntigravityMcpProperties properties;

    @Getter
    private McpSyncClient syncClient;

    private boolean connected = false;
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    @PostConstruct
    public void init() {
        if (!properties.isEnabled()) {
            log.info("MCP Antigravity client dang tat (mcp.antigravity.enabled=false). Chay che do Local Tool fallback.");
            return;
        }

        if (properties.getEndpoint() == null || properties.getEndpoint().isBlank()) {
            log.warn("MCP Antigravity da bat nhung endpoint bi trong. Chay che do Local Tool fallback.");
            return;
        }

        try {
            log.info("Dang khoi tao ket noi MCP Antigravity Client toi endpoint: {}", properties.getEndpoint());

            HttpClientSseClientTransport.Builder transportBuilder = HttpClientSseClientTransport
                    .builder(properties.getEndpoint())
                    .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

            if (properties.getToken() != null && !properties.getToken().isBlank()) {
                transportBuilder.requestBuilder(
                        HttpRequest.newBuilder()
                                .header("Authorization", "Bearer " + properties.getToken().trim())
                );
            }

            HttpClientSseClientTransport transport = transportBuilder.build();
            this.syncClient = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .build();

            McpSchema.InitializeResult initResult = this.syncClient.initialize();
            this.connected = true;
            this.toolCallbackProvider = new SyncMcpToolCallbackProvider(List.of(this.syncClient));

            log.info("Ket noi MCP Antigravity Server thanh cong! Protocol: {}, Server: {} v{}",
                    initResult.protocolVersion(),
                    initResult.serverInfo().name(),
                    initResult.serverInfo().version());
        } catch (Exception e) {
            log.warn("Khong the ket noi toi MCP Antigravity Server ({}: {}). Tu dong fallback su dung Local Tools.",
                    e.getClass().getSimpleName(), e.getMessage());
            this.connected = false;
            this.syncClient = null;
        }
    }

    @PreDestroy
    public void close() {
        if (this.syncClient != null) {
            try {
                this.syncClient.close();
                log.info("Da dong ket noi MCP Antigravity Client.");
            } catch (Exception e) {
                log.warn("Loi khi dong MCP Antigravity Client: {}", e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return this.connected && this.syncClient != null;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public SyncMcpToolCallbackProvider getToolCallbackProvider() {
        return this.toolCallbackProvider;
    }

    public List<ToolCallback> getMcpToolCallbacks() {
        if (!isConnected() || this.toolCallbackProvider == null) {
            return Collections.emptyList();
        }
        try {
            ToolCallback[] callbacks = this.toolCallbackProvider.getToolCallbacks();
            return callbacks != null ? List.of(callbacks) : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Loi khi lay danh sach MCP ToolCallbacks: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public McpStatusDto getStatus() {
        List<String> tools = new ArrayList<>();
        int count = 0;

        if (isConnected()) {
            try {
                McpSchema.ListToolsResult toolsResult = this.syncClient.listTools();
                if (toolsResult != null && toolsResult.tools() != null) {
                    count = toolsResult.tools().size();
                    for (McpSchema.Tool t : toolsResult.tools()) {
                        tools.add(t.name() + ": " + t.description());
                    }
                }
            } catch (Exception e) {
                log.warn("Khong the list tools tu MCP Server: {}", e.getMessage());
            }
        }

        return McpStatusDto.builder()
                .enabled(properties.isEnabled())
                .connected(isConnected())
                .endpoint(properties.getEndpoint())
                .clientType("SSE/HTTP Client")
                .toolCount(count)
                .availableTools(tools)
                .message(isConnected()
                        ? "MCP Antigravity Client da ket noi va san sang"
                        : (properties.isEnabled()
                           ? "MCP bat nhung chua ket noi toi server, dang chay Local Tool fallback"
                           : "MCP tat, dang su dung Local Tool fallback"))
                .build();
    }

    public McpCapabilityHandler getCapabilities() {
        if (!isConnected()) {
            return McpCapabilityHandler.disabled();
        }

        try {
            McpSchema.ServerCapabilities serverCaps = this.syncClient.getServerCapabilities();
            McpSchema.Implementation serverInfo = this.syncClient.getServerInfo();
            McpSchema.ListToolsResult listTools = this.syncClient.listTools();

            List<String> names = listTools != null && listTools.tools() != null
                    ? listTools.tools().stream().map(McpSchema.Tool::name).toList()
                    : Collections.emptyList();

            return McpCapabilityHandler.builder()
                    .supportsTools(serverCaps != null && serverCaps.tools() != null)
                    .supportsResources(serverCaps != null && serverCaps.resources() != null)
                    .supportsPrompts(serverCaps != null && serverCaps.prompts() != null)
                    .supportsLogging(serverCaps != null && serverCaps.logging() != null)
                    .serverName(serverInfo != null ? serverInfo.name() : "Antigravity MCP Server")
                    .serverVersion(serverInfo != null ? serverInfo.version() : "1.0")
                    .toolNames(names)
                    .build();
        } catch (Exception e) {
            log.warn("Loi khi lay MCP capabilities: {}", e.getMessage());
            return McpCapabilityHandler.disabled();
        }
    }
}
