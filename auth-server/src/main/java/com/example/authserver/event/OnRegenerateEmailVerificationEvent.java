package com.example.authserver.event;

import com.example.authserver.entity.EmailVerificationToken;
import com.example.authserver.entity.Users;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Setter
public class OnRegenerateEmailVerificationEvent extends ApplicationEvent {
    private transient UriComponentsBuilder redirectUrl;
    private Users user;
    private transient EmailVerificationToken token;

    public OnRegenerateEmailVerificationEvent(Users user, UriComponentsBuilder redirectUrl, EmailVerificationToken token) {
        super(user);
        this.user = user;
        this.redirectUrl = redirectUrl;
        this.token = token;
    }
}
