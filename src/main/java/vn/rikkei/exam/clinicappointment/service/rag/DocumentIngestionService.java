package vn.rikkei.exam.clinicappointment.service.rag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final MarkdownDocumentChunker chunker;

    public int ingestMultipart(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ingestContent(content);
    }

    public int ingestResource(Resource resource) throws IOException {
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ingestContent(content);
    }

    public int ingestContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Tai lieu rong");
        }
        List<Document> chunks = chunker.chunk(content);
        if (chunks.isEmpty()) {
            return 0;
        }

        List<String> ids = chunks.stream().map(Document::getId).toList();
        try {
            vectorStore.delete(ids);
        } catch (Exception e) {
            log.debug("Khong the xoa hoac chua co record cu trong vector store: {}", e.getMessage());
        }

        vectorStore.add(chunks);
        log.info("Da ingest thanh cong {} chunks vao vector store voi deterministic ID", chunks.size());
        return chunks.size();
    }
}
