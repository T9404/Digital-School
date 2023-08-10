package com.example.authserver.dto;

import lombok.Builder;

@Builder
public record AuthResponse(String accessToken, String refreshToken) {
}
