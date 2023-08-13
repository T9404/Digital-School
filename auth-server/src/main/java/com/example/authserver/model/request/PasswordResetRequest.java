package com.example.authserver.model.request;

import com.example.authserver.validation.annotation.PasswordComplexity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(@Email(message = "The email for which the password needs to be validated") String email,
                                   @NotBlank(message = "New password cannot be blank") @PasswordComplexity String password,
                                   @NotBlank(message = "Confirm Password cannot be blank") @PasswordComplexity String confirmPassword) {
}
