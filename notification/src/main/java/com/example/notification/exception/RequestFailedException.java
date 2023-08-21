package com.example.notification.exception;

public class RequestFailedException extends RuntimeException {
    public RequestFailedException(int code) {
        super("Request failed with response code: " + code);
    }
}
