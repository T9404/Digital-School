package com.example.authserver.model.request;

public record EmailTokenRequest(String email, String code) {
}
