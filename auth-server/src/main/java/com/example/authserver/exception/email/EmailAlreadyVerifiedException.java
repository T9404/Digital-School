package com.example.authserver.exception.email;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException() {
        super("api.custom.exception.email.already-confirmed");
    }
}
