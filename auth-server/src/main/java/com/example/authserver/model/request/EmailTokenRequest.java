package com.example.authserver.model.request;

import jakarta.validation.constraints.Email;

public record EmailTokenRequest(@Email String email, String code) {
}
