package com.example.authserver.model.request;

import com.example.authserver.validation.annotation.PasswordComplexity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(@Email(message = "api.warning.email.validate")
                                   String email,

                                   @NotBlank(message = "api.warning.password-not-empty")
                                   @PasswordComplexity String password,

                                   @NotBlank(message = "api.warning.password-not-equals")
                                   @PasswordComplexity String confirmPassword) {
}
