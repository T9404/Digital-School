package com.example.authserver.model.request;

import com.example.authserver.validation.annotation.PasswordComplexity;
import jakarta.validation.constraints.Email;

public record AuthRequest(@Email String email,
                          @PasswordComplexity String password) {
}
