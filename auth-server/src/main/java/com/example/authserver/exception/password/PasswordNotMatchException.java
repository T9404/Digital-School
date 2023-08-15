package com.example.authserver.exception.password;

import com.example.authserver.util.MessageUtil;

public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException() {
        super(MessageUtil.getMessage("exception.password.not-equals-to-confirm"));
    }
}
