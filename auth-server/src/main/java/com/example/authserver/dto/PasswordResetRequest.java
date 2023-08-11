package com.example.authserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(@Email(message = "The email for which the password needs to be validated") String email,
                                   @NotBlank(message = "New password cannot be blank") String password,
                                   @NotBlank(message = "Confirm Password cannot be blank") String confirmPassword,
                                   @NotBlank(message = "Password reset token for the specified email has to be supplied") String token) {
}
