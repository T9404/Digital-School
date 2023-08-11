package com.example.authserver.enums;

import lombok.Getter;

@Getter
public enum DefaultStatus {
    SUCCESS("success"),
    ERROR("error");

    private final String status;

    DefaultStatus(String status) {
        this.status = status;
    }
}
