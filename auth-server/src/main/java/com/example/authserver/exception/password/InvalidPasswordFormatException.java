package com.example.authserver.exception.password;

public class InvalidPasswordFormatException extends RuntimeException {
    public InvalidPasswordFormatException() {
        super("api.custom.exception.password.invalid");
    }
}
