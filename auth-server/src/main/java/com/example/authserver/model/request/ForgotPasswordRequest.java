package com.example.authserver.model.request;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(@Email String email) {
}
