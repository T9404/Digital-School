package com.example.authserver.model.response;

import lombok.Builder;

@Builder
public record UserResponse(String username,
                           String email,
                           boolean isVerified) {
}
