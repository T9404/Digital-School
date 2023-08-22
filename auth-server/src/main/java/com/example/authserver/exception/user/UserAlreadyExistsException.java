package com.example.authserver.exception.user;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("api.custom.exception.user.already-exists");
    }
}