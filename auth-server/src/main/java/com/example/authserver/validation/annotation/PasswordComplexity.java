package com.example.authserver.validation.annotation;

import com.example.authserver.validation.validator.PasswordComplexityValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordComplexityValidator.class)
public @interface PasswordComplexity {
    String message() default "api.warning.password.complexity";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
