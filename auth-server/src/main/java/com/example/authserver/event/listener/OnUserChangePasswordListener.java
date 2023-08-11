/*
package com.example.authserver.event.listener;

import com.example.authserver.entity.Users;
import com.example.authserver.event.OnUserChangePasswordEvent;
import com.example.authserver.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OnUserChangePasswordListener implements ApplicationListener<OnUserChangePasswordEvent> {
    private MailService mailService;

    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Override
    @Async
    public void onApplicationEvent(OnUserChangePasswordEvent event) {
        sendChangePassword(event);
    }

    private void sendChangePassword(OnUserChangePasswordEvent event) {
        Users user = event.getUser();
        String action = event.getAction();
        String actionStatus = event.getActionStatus();
        String recipientAddress = user.getEmail();
        try {
            mailService.sendChangePassword(action, actionStatus, recipientAddress);
        }
    }
}
*/
