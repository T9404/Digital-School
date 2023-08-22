package com.example.authserver.model.request;

import com.example.authserver.validation.annotation.PasswordComplexity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(String name,

                              @Email(message = "api.warning.email.validate")
                              String email,

                              @NotBlank(message = "api.warning.password-not-empty")
                              @PasswordComplexity
                              String password) {
}
