package com.example.authserver.event;

import com.example.authserver.entity.Users;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class OnUserChangePasswordEvent extends ApplicationEvent {
    private Users user;
    private String action;
    private String actionStatus;

    public OnUserChangePasswordEvent(Users user, String action, String actionStatus) {
        super(user);
        this.user = user;
        this.action = action;
        this.actionStatus = actionStatus;
    }
}
