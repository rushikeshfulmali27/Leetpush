package com.leethubai.common.exception;

import com.leethubai.common.dto.ValidationDTOs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ValidationDTOs.ApiError> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[{}] Resource not found: {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ValidationDTOs.ApiError.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("NOT_FOUND")
                        .code("RESOURCE_NOT_FOUND")
                        .message(ex.getMessage())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .traceId(traceId)
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("[{}] Validation failed: {}", traceId, errors);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Input validation failed");
        response.put("errors", errors);
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("traceId", traceId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(SyncException.class)
    public ResponseEntity<ValidationDTOs.ApiError> handleSyncException(
            SyncException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Sync error: {}", traceId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ValidationDTOs.ApiError.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .error("UNPROCESSABLE_ENTITY")
                        .code("SYNC_ERROR")
                        .message(ex.getMessage())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .traceId(traceId)
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ValidationDTOs.ApiError> handleRateLimitExceeded(
            RateLimitExceededException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[{}] Rate limit exceeded: {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ValidationDTOs.ApiError.builder()
                        .status(HttpStatus.TOO_MANY_REQUESTS.value())
                        .error("TOO_MANY_REQUESTS")
                        .code("RATE_LIMIT_EXCEEDED")
                        .message(ex.getMessage())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .traceId(traceId)
                        .timestamp(System.currentTimeMillis())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationDTOs.ApiError> handleGenericException(
            Exception ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("[{}] Unexpected error", traceId, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ValidationDTOs.ApiError.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_SERVER_ERROR")
                        .code("INTERNAL_ERROR")
                        .message("An unexpected error occurred")
                        .path(request.getDescription(false).replace("uri=", ""))
                        .traceId(traceId)
                        .timestamp(System.currentTimeMillis())
                        .build());
    }
}
