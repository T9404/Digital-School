package com.example.authserver.service.implementation;

import com.example.authserver.service.MailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailVerification(String emailConfirmationUrl, String recipientAddress) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject("Registration Confirmation");
        email.setText("To confirm your e-mail address, please click the link below:\n" + emailConfirmationUrl);
        mailSender.send(email);
    }

    @Override
    public void sendResetPasswordLink(String emailConfirmationUrl, String recipientAddress) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject("Reset Password");
        email.setText("To reset your password, please click the link below:\n" + emailConfirmationUrl);
        mailSender.send(email);
    }
}
