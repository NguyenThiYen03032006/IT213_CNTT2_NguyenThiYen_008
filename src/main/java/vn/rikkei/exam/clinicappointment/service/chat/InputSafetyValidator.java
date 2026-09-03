package vn.rikkei.exam.clinicappointment.service.chat;

import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.rikkei.exam.clinicappointment.exception.SecurityValidationException;

@Slf4j
@Component
public class InputSafetyValidator {

    private static final List<Pattern> PROMPT_INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore (all )?(previous|prior) (instructions|directions|prompts)"),
            Pattern.compile("(?i)disregard (all )?(previous|prior) (instructions|directions)"),
            Pattern.compile("(?i)(reveal|show|print|display|dump) (the )?(system prompt|hidden prompt)"),
            Pattern.compile("(?i)bo qua (moi )?chi dan truoc"),
            Pattern.compile("(?i)hien thi (system prompt|prompt he thong)")
    );

    private static final List<Pattern> SENSITIVE_INFO_PATTERNS = List.of(
            Pattern.compile("(?i)(reveal|show|in ra|lay|cho biet|xem) .*(token|api[-_ ]?key|secret[-_ ]?key|password|mat khau)"),
            Pattern.compile("(?i)(bien moi truong|environment variable|env variable|db_password|db_url)")
    );

    public void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        String normalized = message.trim();

        for (Pattern pattern : PROMPT_INJECTION_PATTERNS) {
            if (pattern.matcher(normalized).find()) {
                log.warn("Phat hien prompt injection trong input: {}", normalized);
                throw new SecurityValidationException("Phat hien hanh vi tan cong Prompt Injection. Yeu cau bi tu choi.");
            }
        }

        for (Pattern pattern : SENSITIVE_INFO_PATTERNS) {
            if (pattern.matcher(normalized).find()) {
                log.warn("Phat hien hanh vi truy cap du lieu nhay cam: {}", normalized);
                throw new SecurityValidationException("Khong duoc phep truy van thong tin cau hinh, token hoac mat khau.");
            }
        }
    }
}
