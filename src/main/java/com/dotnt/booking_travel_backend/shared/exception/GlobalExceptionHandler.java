package com.dotnt.booking_travel_backend.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(status.value())
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(status.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(status.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage() != null ? ex.getMessage() : "Access Denied")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() == null ? "Unexpected error" : ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Handle S3-specific errors
        if (message.startsWith("S3_UNAVAILABLE")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = message.substring("S3_UNAVAILABLE:".length()).trim();
        } else if (message.startsWith("S3_ERROR")) {
            // keep default INTERNAL_SERVER_ERROR
            message = message.substring("S3_ERROR:".length()).trim();
        }

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }
}
