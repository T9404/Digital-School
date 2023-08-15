package com.example.authserver.exception.email;

public class EmailAlreadyConfirmedException extends RuntimeException {
    public EmailAlreadyConfirmedException() {
        super("Email already confirmed");
    }
}
