package vn.rikkei.exam.clinicappointment.service.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import vn.rikkei.exam.clinicappointment.dto.ChatResponse;
import vn.rikkei.exam.clinicappointment.service.langfuse.LangfuseService;
import vn.rikkei.exam.clinicappointment.service.mcp.AntigravityMcpService;
import vn.rikkei.exam.clinicappointment.tool.ReservationTools;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssistantService {

    public static final String FALLBACK_MESSAGE = "Không đủ căn cứ trong tài liệu nội bộ.";

    private static final String SYSTEM_PROMPT =
            """
            Ban la Tro ly AI Dat lich kham noi bo .
            Nhiem vu cua ban la ho tro nguoi dung ve quy dinh, chinh sach va nghiep vu dat lich kham.
            
            Ban tra loi dua tren:
            1) Chinh sach va tieu chuan trong tai lieu noi bo (RAG).
            2) Du lieu thuc te tra ve tu cac cong cu nghiep vu (Agent Tools) hoac he thong MCP.
            
            Quy tac bat buoc:
            - Khong duoc tu suy doan availability, khong duoc tu sinh requestId, khong tu suy dien trang thai lich.
            - Khi tra loi dua tren tai lieu RAG, neu co thong tin, hay giai thich ro rang va kem trich dan nguon.
            - Neu tai lieu noi bo khong co du can cu va khong co cong cu phu hop, Bat buoc phai tra loi chinh xac:
              "Không đủ căn cứ trong tài liệu nội bộ."
            - Khong tiet lo token, password, API key, bien moi truong hay credential he thong.
            - Tra loi bang tieng Viet, ro rang, chuyen nghiep.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final VectorStore vectorStore;
    private final ReservationTools reservationTools;
    private final AntigravityMcpService mcpService;
    private final InputSafetyValidator inputSafetyValidator;
    private final LangfuseService langfuseService;

    public ChatResponse chat(String message, String conversationId) {
        inputSafetyValidator.validateMessage(message);

        String sessionId = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId.trim();

        ToolExecutionTracker.clear();

        long startTime = System.currentTimeMillis();
        String answer = null;
        List<String> sources = new ArrayList<>();
        List<String> toolsUsed = Collections.emptyList();

        try {
            try {
                SearchRequest searchRequest = SearchRequest.builder()
                        .query(message)
                        .topK(4)
                        .similarityThreshold(0.5)
                        .build();
                List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);
                if (retrievedDocs != null) {
                    for (Document doc : retrievedDocs) {
                        if (doc.getMetadata() != null && doc.getMetadata().containsKey("source")) {
                            String src = String.valueOf(doc.getMetadata().get("source"));
                            if (!sources.contains(src)) {
                                sources.add(src);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Khong the truy van similarity search tu vector store: {}", e.getMessage());
            }

            var promptSpec = chatClientBuilder
                    .build()
                    .prompt()
                    .advisors(questionAnswerAdvisor)
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, sessionId))
                    .tools(reservationTools);

            if (mcpService.isConnected() && mcpService.getToolCallbackProvider() != null) {
                log.info("MCP Antigravity hoat dong, tich hop MCP tools vao ChatClient");
                promptSpec.tools(mcpService.getToolCallbackProvider());
            }
            answer = promptSpec
                    .system(SYSTEM_PROMPT)
                    .user(message)
                    .call()
                    .content();

            toolsUsed = ToolExecutionTracker.getToolsUsed();

            if (answer == null || answer.isBlank()) {
                answer = FALLBACK_MESSAGE;
            } else if (sources.isEmpty() && toolsUsed.isEmpty()) {
                String lower = answer.toLowerCase();
                if (lower.contains("không đủ căn cứ") || lower.contains("khong du can cu")
                        || lower.contains("không tìm thấy") || lower.contains("khong tim thay")
                        || lower.contains("tôi không có thông tin") || lower.contains("tai lieu khong co")) {
                    answer = FALLBACK_MESSAGE;
                }
            }

        } catch (Exception e) {
            log.error("Loi trong qua trinh xu ly chat: {}", e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String traceId = "tr-" + UUID.randomUUID().toString().substring(0, 8);
            langfuseService.logTrace(
                    traceId,
                    "ClinicAppointmentAssistant",
                    message,
                    answer != null ? answer : "ERROR",
                    duration,
                    sessionId,
                    toolsUsed,
                    sources);

            ToolExecutionTracker.clear();
        }

        return ChatResponse.builder()
                .answer(answer)
                .conversationId(sessionId)
                .sources(sources)
                .toolsUsed(toolsUsed)
                .build();
    }
}
