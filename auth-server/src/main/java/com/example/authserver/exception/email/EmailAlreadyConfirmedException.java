package com.example.authserver.exception.email;

import com.example.authserver.util.MessageUtil;

public class EmailAlreadyConfirmedException extends RuntimeException {
    public EmailAlreadyConfirmedException() {
        super(MessageUtil.getMessage("exception.email.already-confirmed"));
    }
}
