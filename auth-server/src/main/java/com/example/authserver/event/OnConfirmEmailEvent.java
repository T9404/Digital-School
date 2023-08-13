package com.example.authserver.event;

import com.example.authserver.entity.Users;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Setter
public class OnConfirmEmailEvent extends ApplicationEvent {
    private transient UriComponentsBuilder redirectUrl;
    private Users user;
    private String emailAddress;

    public OnConfirmEmailEvent(Users user, UriComponentsBuilder redirectUrl, String emailAddress) {
        super(user);
        this.user = user;
        this.redirectUrl = redirectUrl;
        this.emailAddress = emailAddress;
    }
}
