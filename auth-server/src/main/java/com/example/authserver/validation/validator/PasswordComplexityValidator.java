package com.example.authserver.validation.validator;

import com.example.authserver.validation.annotation.PasswordComplexity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordComplexityValidator implements ConstraintValidator<PasswordComplexity, String> {
    private static final String COMPLEXITY_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password.matches(COMPLEXITY_PATTERN);
    }
}
