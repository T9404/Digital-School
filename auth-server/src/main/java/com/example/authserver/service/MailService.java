package com.example.authserver.service;

public interface MailService {
    void sendEmailVerification(String emailConfirmationUrl, String recipientAddress);
    void sendResetPasswordLink(String emailConfirmationUrl, String recipientAddress);
}
