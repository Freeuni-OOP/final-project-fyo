package com.fyo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fyo.auth.AuthenticatedUserPrincipal;
import com.fyo.auth.CurrentUserService;
import com.fyo.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.server.ResponseStatusException;

class StompAuthChannelInterceptorTests {

    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final StompAuthChannelInterceptor interceptor = new StompAuthChannelInterceptor(currentUserService);
    private final MessageChannel channel = mock(MessageChannel.class);

    @Test
    void connectWithBearerTokenSetsSessionPrincipal() {
        User user = new User("uid-1", "Ana", "Kobalia", "ana", "ana@example.com");
        when(currentUserService.requireCurrentUser("Bearer id-token")).thenReturn(user);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer id-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor updated = StompHeaderAccessor.wrap(result);
        assertThat(updated.getUser()).isInstanceOf(AuthenticatedUserPrincipal.class);
        assertThat(updated.getUser().getName()).isEqualTo("ana");
    }

    @Test
    void connectWithoutTokenIsRejected() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(currentUserService.requireCurrentUser(null))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token"));

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void sendWithoutPrincipalIsRejected() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not authenticated");
    }

    @Test
    void sendWithPrincipalIsAllowed() {
        User user = new User("uid-2", "Nika", "Beridze", "nika", "nika@example.com");
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setUser(new AuthenticatedUserPrincipal(user));
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }
}
