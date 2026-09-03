package vn.rikkei.exam.clinicappointment.service.rag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
public class MarkdownDocumentChunker {

    private static final Pattern SECTION_PATTERN = Pattern.compile("(?m)^#{1,3}\\s+(?:\\d+\\.\\s*)?(.+)$");
    public static final String DOC_ID = "internal-clinic-handbook";

    public List<Document> chunk(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return List.of();
        }

        List<Document> rawDocuments = new ArrayList<>();
        Matcher matcher = SECTION_PATTERN.matcher(markdown);
        List<Integer> starts = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        while (matcher.find()) {
            starts.add(matcher.start());
            titles.add(matcher.group(1).trim());
        }

        if (starts.isEmpty()) {
            rawDocuments.add(createDocument(markdown.trim(), "clinic-policy#general", "Tong quan", "general"));
        } else {
            if (starts.get(0) > 0) {
                String intro = markdown.substring(0, starts.get(0)).trim();
                if (!intro.isBlank()) {
                    rawDocuments.add(createDocument(intro, "clinic-policy#intro", "Gioi thieu", "intro"));
                }
            }

            for (int i = 0; i < starts.size(); i++) {
                int start = starts.get(i);
                int end = i + 1 < starts.size() ? starts.get(i + 1) : markdown.length();
                String sectionContent = markdown.substring(start, end).trim();
                String title = titles.get(i);
                String source = mapSource(title);
                String section = slugify(title);
                rawDocuments.add(createDocument(sectionContent, source, title, section));
            }
        }

        return rawDocuments;
    }

    private Document createDocument(String content, String source, String title, String section) {
        String contentHash = sha256(content);
        String deterministicId = UUID.nameUUIDFromBytes((DOC_ID + ":" + section + ":" + contentHash).getBytes(StandardCharsets.UTF_8)).toString();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("doc_id", DOC_ID);
        metadata.put("source", source);
        metadata.put("title", title);
        metadata.put("section", section);
        metadata.put("content_hash", contentHash);
        metadata.put("category", "clinic-policy");

        return new Document(deterministicId, content, metadata);
    }

    private String mapSource(String title) {
        String normalized = title.toLowerCase();
        if (normalized.contains("tiêu chuẩn") || normalized.contains("tieu chuan") || normalized.contains("tiêu chuẩn lịch khám")) {
            return "clinic-policy#standards";
        }
        if (normalized.contains("chính sách") || normalized.contains("chinh sach") || normalized.contains("khám sức khỏe")) {
            return "clinic-policy#health-check-policy";
        }
        if (normalized.contains("hủy") || normalized.contains("huy") || normalized.contains("phê duyệt") || normalized.contains("phe duyet") || normalized.contains("duyệt")) {
            return "clinic-policy#approval-cancellation";
        }
        return "clinic-policy#" + slugify(title);
    }

    private String slugify(String text) {
        if (text == null) return "section";
        return text.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
