package com.example.authserver.exception.token;

import com.example.authserver.util.MessageUtil;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException() {
        super(MessageUtil.getMessage("exception.token.already-used"));
    }
}
