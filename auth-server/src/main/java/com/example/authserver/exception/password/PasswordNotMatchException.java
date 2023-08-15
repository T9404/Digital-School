package com.example.authserver.exception.password;

public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException() {
        super("Passwords do not match");
    }
}
