package com.example.authserver.exception.token;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException() {
        super("Token already used");
    }
}
