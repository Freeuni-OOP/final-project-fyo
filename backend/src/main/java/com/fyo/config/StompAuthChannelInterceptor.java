package com.fyo.config;

import com.fyo.auth.AuthenticatedUserPrincipal;
import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT frames with the same Firebase Bearer token used
 * on REST. The resolved user becomes the session principal for SEND/SUBSCRIBE.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final CurrentUserService currentUserService;

    public StompAuthChannelInterceptor(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (!accessor.isMutable()) {
            accessor = StompHeaderAccessor.wrap(message);
        }
        accessor.setLeaveMutable(true);

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        if (command == StompCommand.CONNECT) {
            User user = currentUserService.requireCurrentUser(accessor.getFirstNativeHeader("Authorization"));
            accessor.setUser(new AuthenticatedUserPrincipal(user));
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (requiresAuthentication(command) && accessor.getUser() == null) {
            throw new IllegalStateException("STOMP session is not authenticated");
        }

        return message;
    }

    private static boolean requiresAuthentication(StompCommand command) {
        return command == StompCommand.SEND
                || command == StompCommand.SUBSCRIBE
                || command == StompCommand.UNSUBSCRIBE;
    }
}
