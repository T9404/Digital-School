package com.example.authserver.exception.password;

import com.example.authserver.util.MessageUtil;

public class PasswordAlreadyUsedException extends RuntimeException {
    public PasswordAlreadyUsedException() {
        super(MessageUtil.getMessage("exception.password.already-used"));
    }
}
