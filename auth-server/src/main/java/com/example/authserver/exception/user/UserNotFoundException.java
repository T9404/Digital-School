package com.example.authserver.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("api.custom.exception.user.not-found");
    }
}
