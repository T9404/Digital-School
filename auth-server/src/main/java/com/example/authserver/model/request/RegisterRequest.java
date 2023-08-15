package com.example.authserver.model.request;

import jakarta.validation.constraints.Email;

public record RegisterRequest(String name, @Email String email, String password) {
}
