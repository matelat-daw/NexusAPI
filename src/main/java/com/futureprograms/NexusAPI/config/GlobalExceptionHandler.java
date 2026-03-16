package com.futureprograms.NexusAPI.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing + "; " + replacement
                ));

        Map<String, Object> response = buildErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                errors
        );
        
        logger.warn("Validation error: {}", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> response = buildErrorResponse(
                "Invalid argument: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                null
        );
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        logger.warn("Max upload size exceeded: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
                "La imagen es demasiado grande. El límite es 10MB.",
                HttpStatus.PAYLOAD_TOO_LARGE,
                null
        );
        
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error: ", ex);
        
        Map<String, Object> response = buildErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> buildErrorResponse(String message, HttpStatus status, Map<String, String> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.value());
        response.put("timestamp", LocalDateTime.now());
        if (errors != null) {
            response.put("details", errors);
        }
        return response;
    }
}

