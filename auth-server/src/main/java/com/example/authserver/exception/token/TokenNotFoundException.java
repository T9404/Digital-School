package com.example.authserver.exception.token;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {
        super("Token not found");
    }
}
