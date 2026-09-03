package vn.rikkei.exam.clinicappointment.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("ResourceNotFoundException tai {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        log.warn("BusinessRuleException tai {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(SecurityValidationException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityViolation(SecurityValidationException ex, HttpServletRequest request) {
        log.warn("SecurityValidationException tai {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "SECURITY_VIOLATION", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("IllegalArgumentException tai {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Du lieu khong hop le");
        log.warn("MethodArgumentNotValidException tai {}: {}", request.getRequestURI(), message);
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Internal Server Error tai {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                        "Da xay ra loi noi bo he thong. Vui long thu lai sau.", request.getRequestURI()));
    }

    private Map<String, Object> buildError(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", sanitize(message));
        body.put("path", path);
        return body;
    }

    private String sanitize(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("(?i)(password|secret|key|token)=[^\\s,&]+", "$1=***");
    }
}
