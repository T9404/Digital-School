package com.example.authserver.exception.email;

import com.example.authserver.util.MessageUtil;

public class EmailNotConfirmedException extends RuntimeException {
    public EmailNotConfirmedException() {
        super(MessageUtil.getMessage("exception.email.not-confirmed"));
    }
}
