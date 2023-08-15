package com.example.authserver.exception.password;

public class PasswordUsedException extends RuntimeException {
    public PasswordUsedException() {
        super("Password already used");
    }
}
