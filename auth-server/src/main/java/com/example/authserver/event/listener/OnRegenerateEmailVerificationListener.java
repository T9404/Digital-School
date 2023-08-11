package com.example.authserver.event.listener;

import com.example.authserver.entity.Users;
import com.example.authserver.event.OnRegenerateEmailVerificationEvent;
import com.example.authserver.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnRegenerateEmailVerificationListener implements ApplicationListener<OnRegenerateEmailVerificationEvent> {
    private MailService mailService;

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnRegenerateEmailVerificationEvent event) {
        resendEmailVerification(event);
    }

    private void resendEmailVerification(OnRegenerateEmailVerificationEvent event) {
        Users user = event.getUser();
        String recipientAddress = user.getEmail();
        String emailConfirmationUrl = event.getRedirectUrl().queryParam("token", event.getToken()).toUriString();
        try {
            mailService.sendEmailVerification(emailConfirmationUrl, recipientAddress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
