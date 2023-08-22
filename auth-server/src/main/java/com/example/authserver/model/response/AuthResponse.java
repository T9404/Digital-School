package com.example.authserver.model.response;

import lombok.Builder;

@Builder
public record AuthResponse(String accessToken,
                           String refreshToken) {
}
