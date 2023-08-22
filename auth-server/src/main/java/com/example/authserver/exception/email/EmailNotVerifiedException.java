package com.example.authserver.exception.email;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("api.custom.exception.email.not-confirmed");
    }
}
