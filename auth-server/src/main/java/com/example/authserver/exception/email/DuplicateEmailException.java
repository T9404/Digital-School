package com.example.authserver.exception.email;

import com.example.authserver.util.MessageUtil;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super(MessageUtil.getMessage("exception.email.equals-to-current"));
    }
}
