package com.example.authserver.exception.user;

import com.example.authserver.util.MessageUtil;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(MessageUtil.getMessage("exception.user.not-found"));
    }
}
