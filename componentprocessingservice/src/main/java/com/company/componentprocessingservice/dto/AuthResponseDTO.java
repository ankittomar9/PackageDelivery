package com.company.componentprocessingservice.dto;

public record AuthResponseDTO(
        String jwtToken,
        String refreshToken,
        String tokenType,
        Long expiresInMs,
        String username,
        Boolean valid
) {
    public AuthResponseDTO(String jwtToken, Boolean valid) {
        this(jwtToken, null, "Bearer", 1800000L, null, valid);
    }
}