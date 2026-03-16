package com.futureprograms.NexusAPI.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private String message;
    private boolean success;
    private LocalDateTime timestamp;
    private Integer statusCode;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .success(true)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
        return ApiResponse.<T>builder()
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .build();
    }
}

