package com.example.authserver.exception.password;

public class PasswordAlreadyUsedException extends RuntimeException {
    public PasswordAlreadyUsedException() {
        super("api.custom.exception.password.already-used");
    }
}
