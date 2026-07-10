package com.fyo.auth;

import com.fyo.domain.User;
import java.security.Principal;

/** Principal attached to a STOMP session after a successful CONNECT. */
public class AuthenticatedUserPrincipal implements Principal {

    private final Long userId;
    private final String name;

    public AuthenticatedUserPrincipal(User user) {
        this.userId = user.getId();
        this.name = user.getUsername() != null ? user.getUsername() : String.valueOf(user.getId());
    }

    public Long userId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }
}
