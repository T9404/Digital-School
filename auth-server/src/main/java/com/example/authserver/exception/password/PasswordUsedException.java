package com.example.authserver.exception.password;

import com.example.authserver.util.MessageUtil;

public class PasswordUsedException extends RuntimeException {
    public PasswordUsedException() {
        super(MessageUtil.getMessage("exception.password.already-used"));
    }
}
