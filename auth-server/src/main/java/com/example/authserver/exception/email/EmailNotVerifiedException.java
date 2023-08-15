package com.example.authserver.exception.email;

import com.example.authserver.util.MessageUtil;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super(MessageUtil.getMessage("exception.email.not-confirmed"));
    }
}
