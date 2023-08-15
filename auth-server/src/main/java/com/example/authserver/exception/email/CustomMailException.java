package com.example.authserver.exception.email;

public class CustomMailException extends RuntimeException {
    public CustomMailException(String message) {
        super(message);
    }
}
