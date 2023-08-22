package com.example.authserver.event.listener;

import com.example.authserver.entity.PasswordToken;
import com.example.authserver.entity.Users;
import com.example.authserver.event.OnCreatePwdResetLinkEvent;
import com.example.authserver.exception.email.CustomEmailException;
import com.example.authserver.service.implementation.MailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnCreatePwdResetLinkListener implements ApplicationListener<OnCreatePwdResetLinkEvent> {
    private final MailServiceImpl mailService;

    @Autowired
    public OnCreatePwdResetLinkListener(MailServiceImpl mailService) {
        this.mailService = mailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnCreatePwdResetLinkEvent event) {
        sendResetLink(event);
    }

    private void sendResetLink(OnCreatePwdResetLinkEvent event) {
        try {
            PasswordToken passwordToken = event.getPasswordToken();
            Users user = passwordToken.getUser();
            String recipientAddress = user.getEmail();
            String emailConfirmationUrl = event.getRedirectUrl().queryParam("token", passwordToken.getToken()).toUriString();
            mailService.sendResetPasswordLink(emailConfirmationUrl, recipientAddress);
        } catch (Exception exception) {
            throw new CustomEmailException();
        }
    }
}
