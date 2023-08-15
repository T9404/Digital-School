package com.example.authserver.exception.email;

import com.example.authserver.util.MessageUtil;

public class EmailsSameException extends RuntimeException {
    public EmailsSameException() {
        super(MessageUtil.getMessage("exception.email.equals-to-current"));
    }
}
