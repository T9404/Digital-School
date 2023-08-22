package com.example.authserver.exception.email;

public class CustomEmailException extends RuntimeException {
    public CustomEmailException() {
        super("api.custom.exception.email.send");
    }
}
