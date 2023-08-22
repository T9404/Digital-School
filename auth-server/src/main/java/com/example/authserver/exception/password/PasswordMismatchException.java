package com.example.authserver.exception.password;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("api.custom.exception.password.not-equals-to-confirm");
    }
}
