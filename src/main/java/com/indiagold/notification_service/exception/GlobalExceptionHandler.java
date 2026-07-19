package com.indiagold.notification_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 — User Not Found ───────────────────────────────────────
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
            UserNotFoundException ex) {

        log.warn("User not found — userId={}", ex.getUserId());

        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }
    
    // ─── 400 — Missing Request Body ─────────────────────────────
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleMissingBody(
                HttpMessageNotReadableException ex) {

        log.warn("Request body is missing or unreadable — {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Request body is missing or malformed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
        }

    // ─── 400 — Validation Failed ────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed — errors={}", fieldErrors);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("details", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    // ─── 400 — Invalid Enum Value ───────────────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        log.warn("Invalid value provided — field={}, value={}",
                ex.getName(), ex.getValue());

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid value: " + ex.getValue()
                + " is not a valid channel type");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    // ─── 500 — Unexpected Errors ────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {

        log.error("Unexpected error occurred — message={}", ex.getMessage(), ex);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "An unexpected error occurred. Please try again later.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}