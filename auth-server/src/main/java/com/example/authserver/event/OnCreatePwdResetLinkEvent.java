package com.example.authserver.event;

import com.example.authserver.entity.PasswordToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Setter
public class OnCreatePwdResetLinkEvent extends ApplicationEvent {
    private transient UriComponentsBuilder redirectUrl;
    private transient PasswordToken passwordToken;

    public OnCreatePwdResetLinkEvent(PasswordToken passwordToken, UriComponentsBuilder redirectUrl) {
        super(passwordToken);
        this.passwordToken = passwordToken;
        this.redirectUrl = redirectUrl;
    }
}
