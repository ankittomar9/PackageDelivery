package com.company.authorizationservice.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int statusCode,
        String message,
        String errorDetails,
        LocalDateTime timestamp
) {
    public ErrorResponseDTO(int statusCode, String message, String errorDetails) {
        this(statusCode, message, errorDetails, LocalDateTime.now());
    }
}