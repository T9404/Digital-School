package com.example.authserver.event.listener;

import com.example.authserver.entity.Users;
import com.example.authserver.event.OnConfirmEmailEvent;
import com.example.authserver.service.EmailCodeService;
import com.example.authserver.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnConfirmEmailListener implements ApplicationListener<OnConfirmEmailEvent> {
    private EmailCodeService emailCodeService;
    private MailService mailService;

    @Autowired
    public void setEmailVerificationTokenService(EmailCodeService emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnConfirmEmailEvent onConfirmEmailEvent) {
        sendEmailVerification(onConfirmEmailEvent);
    }

    private void sendEmailVerification(OnConfirmEmailEvent event) {
        Users user = event.getUser();
        String token = emailCodeService.generateNewToken();
        emailCodeService.createCode(user, token);
        String recipientAddress = event.getEmailAddress();
        String emailConfirmationUrl = event
                .getRedirectUrl()
                .queryParam("token", token)
                .queryParam("email", recipientAddress)
                .toUriString();
        try {
            mailService.sendEmailVerification(emailConfirmationUrl, recipientAddress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
