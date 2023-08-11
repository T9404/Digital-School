package com.example.authserver.event.listener;

import com.example.authserver.entity.PasswordResetToken;
import com.example.authserver.entity.Users;
import com.example.authserver.event.OnCreateResetLinkEvent;
import com.example.authserver.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnCreateResetLinkListener implements ApplicationListener<OnCreateResetLinkEvent> {
    private final MailService mailService;

    @Autowired
    public OnCreateResetLinkListener(MailService mailService) {
        this.mailService = mailService;
    }


    @Override
    @Async
    public void onApplicationEvent(OnCreateResetLinkEvent event) {
        sendResetLink(event);
    }

    private void sendResetLink(OnCreateResetLinkEvent event) {
        PasswordResetToken passwordResetToken = event.getPasswordResetToken();
        Users user = passwordResetToken.getUser();
        String recipientAddress = user.getEmail();
        String emailConfirmationUrl = event.getRedirectUrl().queryParam("token", passwordResetToken.getToken())
                .toUriString();
        try {
            mailService.sendResetPasswordLink(emailConfirmationUrl, recipientAddress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
