package com.example.authserver.exception.user;

import com.example.authserver.util.MessageUtil;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super(MessageUtil.getMessage("exception.user.already-exists"));
    }
}