package com.example.authserver.dto;

import lombok.Builder;

@Builder
public record RegisterResponse(String message, String status) {
}
