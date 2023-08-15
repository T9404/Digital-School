package com.example.authserver.exception.password;

import com.example.authserver.util.MessageUtil;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super(MessageUtil.getMessage("exception.password.invalid"));
    }
}
