package com.fyo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * STOMP send-message payload. Still carries the sender id because the
 * WebSocket handshake is not authenticated yet; once a CONNECT interceptor
 * establishes a principal, the sender must come from it and this field goes
 * away. Until then the REST endpoints are the trusted path.
 */
public record SocketChatMessagePayload(
        @NotNull Long senderUserId,
        @NotBlank @Size(max = 4000) String body
) {
}
