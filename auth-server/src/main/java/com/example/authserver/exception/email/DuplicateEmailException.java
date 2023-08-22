package com.example.authserver.exception.email;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("api.custom.exception.email.equals");
    }
}
