package com.example.authserver.exception.email;

public class EmailsSameException extends RuntimeException {
    public EmailsSameException() {
        super("Emails must be different");
    }
}
