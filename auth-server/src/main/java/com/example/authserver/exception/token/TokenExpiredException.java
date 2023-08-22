package com.example.authserver.exception.token;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("api.custom.exception.token.expired");
    }
}
