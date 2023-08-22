package com.example.authserver.enums;

import lombok.Getter;

@Getter
public enum DefaultStatus {
    SUCCESS("api.default.status.success"),
    ERROR("api.default.status.error");

    private final String status;

    DefaultStatus(String status) {
        this.status = status;
    }
}
