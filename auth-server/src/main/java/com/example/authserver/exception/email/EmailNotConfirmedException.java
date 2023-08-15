package com.example.authserver.exception.email;

public class EmailNotConfirmedException extends RuntimeException {
    public EmailNotConfirmedException() {
        super("Email not confirmed");
    }
}
