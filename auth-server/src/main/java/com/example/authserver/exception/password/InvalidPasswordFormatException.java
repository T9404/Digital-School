package com.example.authserver.exception.password;

import com.example.authserver.util.MessageUtil;

public class InvalidPasswordFormatException extends RuntimeException {
    public InvalidPasswordFormatException() {
        super(MessageUtil.getMessage("exception.password.invalid"));
    }
}
