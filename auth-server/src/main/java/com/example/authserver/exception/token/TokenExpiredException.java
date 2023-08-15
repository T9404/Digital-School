package com.example.authserver.exception.token;

import com.example.authserver.util.MessageUtil;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super(MessageUtil.getMessage("exception.token.expired"));
    }
}
