package vn.rikkei.exam.clinicappointment.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.rikkei.exam.clinicappointment.dto.IngestResponse;
import vn.rikkei.exam.clinicappointment.service.rag.DocumentIngestionService;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentIngestionService documentIngestionService;

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public IngestResponse ingest(@RequestPart("file") MultipartFile file) throws IOException {
        int chunks = documentIngestionService.ingestMultipart(file);
        return IngestResponse.builder()
                .chunksIngested(chunks)
                .message("Da ingest " + chunks + " chunk vao vector store")
                .build();
    }

    @PostMapping("/ingest-default")
    public IngestResponse ingestDefault() throws IOException {
        int chunks = documentIngestionService.ingestResource(
                new ClassPathResource("tai_lieu_noi_bo.md"));
        return IngestResponse.builder()
                .chunksIngested(chunks)
                .message("Da ingest so tay mac dinh: " + chunks + " chunk")
                .build();
    }


}
