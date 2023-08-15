package com.example.authserver.event.listener;

import com.example.authserver.entity.Users;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.exception.email.CustomMailException;
import com.example.authserver.service.implementation.EmailCodeServiceImpl;
import com.example.authserver.service.implementation.MailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnConfirmEmailListener implements ApplicationListener<OnConfirmEmailEvent> {
    private EmailCodeServiceImpl emailCodeService;
    private MailServiceImpl mailService;

    @Autowired
    public void setEmailVerificationTokenService(EmailCodeServiceImpl emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    @Autowired
    public void setMailService(MailServiceImpl mailService) {
        this.mailService = mailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnConfirmEmailEvent onConfirmEmailEvent) {
        sendEmailVerification(onConfirmEmailEvent);
    }

    private void sendEmailVerification(OnConfirmEmailEvent event) {
        try {
            String token = generateEmailVerificationToken();
            createEmailCode(event.getUser(), token);
            String recipientAddress = event.getNewEmailAddress();
            String emailConfirmationUrl = buildEmailConfirmationUrl(event, token, recipientAddress);
            mailService.sendEmailVerification(emailConfirmationUrl, recipientAddress);
        } catch (Exception exception) {
            throw new CustomMailException(exception.getMessage());
        }
    }

    private String generateEmailVerificationToken() {
        return emailCodeService.generateNewToken();
    }

    private void createEmailCode(Users user, String token) {
        emailCodeService.createCode(user, token);
    }

    private String buildEmailConfirmationUrl(OnConfirmEmailEvent event, String token, String recipientAddress) {
        return event
                .getRedirectUrl()
                .queryParam("token", token)
                .queryParam("email", recipientAddress)
                .toUriString();
    }
}
