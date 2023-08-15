package com.example.authserver.exception.token;

import com.example.authserver.util.MessageUtil;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {
        super(MessageUtil.getMessage("exception.token.not-found"));
    }
}
