package com.example.authserver.exception.password;

import com.example.authserver.util.MessageUtil;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super(MessageUtil.getMessage("exception.password.not-equals-to-confirm"));
    }
}
