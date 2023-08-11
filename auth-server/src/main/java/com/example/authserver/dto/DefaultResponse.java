package com.example.authserver.dto;

import com.example.authserver.enums.DefaultStatus;

public record DefaultResponse(String message, DefaultStatus status) {
}
