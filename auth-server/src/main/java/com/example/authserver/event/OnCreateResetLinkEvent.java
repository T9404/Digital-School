package com.example.authserver.event;

import com.example.authserver.entity.PasswordResetToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Setter
public class OnCreateResetLinkEvent extends ApplicationEvent {
    private transient UriComponentsBuilder redirectUrl;
    private transient PasswordResetToken passwordResetToken;

    public OnCreateResetLinkEvent(PasswordResetToken passwordResetToken, UriComponentsBuilder redirectUrl) {
        super(passwordResetToken);
        this.passwordResetToken = passwordResetToken;
        this.redirectUrl = redirectUrl;
    }
}
