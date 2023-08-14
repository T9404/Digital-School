package com.example.authserver.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailVerification(String emailConfirmationUrl, String recipientAddress) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject("Registration Confirmation");
        email.setText("To confirm your e-mail address, please click the link below:\n"
                + emailConfirmationUrl);
        mailSender.send(email);
    }

    public void sendResetPasswordLink(String emailConfirmationUrl, String recipientAddress) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject("Reset Password");
        email.setText("To reset your password, please click the link below:\n"
                + emailConfirmationUrl);
        mailSender.send(email);
    }
}
