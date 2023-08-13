package com.example.authserver.model.response;

import lombok.Builder;

@Builder
public record RegisterResponse(String message, String status) {
}
