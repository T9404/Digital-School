package com.example.notification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestFailedException extends RuntimeException {
    private static final Logger log = LoggerFactory.getLogger(RequestFailedException.class);

    public RequestFailedException(int code) {
        super("api.custom.exception.request.failure");
        log.error("Request to API failed with code: {}", code);
    }
}
