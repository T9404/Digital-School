package com.example.authserver.exception.email;

import com.example.authserver.util.MessageUtil;

public class EmailAlreadyVerifiedException extends RuntimeException {
    public EmailAlreadyVerifiedException() {
        super(MessageUtil.getMessage("exception.email.already-confirmed"));
    }
}
