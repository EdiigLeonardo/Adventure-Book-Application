package com.pictet.AdventureBookApplication.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, Object>> conflict(IllegalStateException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> tooLarge(MaxUploadSizeExceededException ex) {
        return response(HttpStatus.CONTENT_TOO_LARGE, "Uploaded file exceeds the configured size limit.");
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now(), "status", status.value(), "error", status.getReasonPhrase(), "message", message == null ? "Request failed" : message));
    }
}
