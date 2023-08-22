package com.example.authserver.exception.token;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException() {
        super("api.custom.exception.token.already-used");
    }
}
