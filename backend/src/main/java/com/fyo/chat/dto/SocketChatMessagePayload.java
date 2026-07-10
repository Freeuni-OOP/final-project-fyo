package com.fyo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** STOMP send-message payload. Sender comes from the authenticated session. */
public record SocketChatMessagePayload(
        @NotBlank @Size(max = 4000) String body
) {
}
