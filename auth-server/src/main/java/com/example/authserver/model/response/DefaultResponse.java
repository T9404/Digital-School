package com.example.authserver.model.response;

import com.example.authserver.enums.DefaultStatus;

public record DefaultResponse(String message, DefaultStatus status) {
}
