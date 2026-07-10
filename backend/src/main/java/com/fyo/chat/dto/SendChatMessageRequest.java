package com.fyo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST send-message body. The sender is never taken from the client — it is
 * resolved from the Authorization Bearer token by the controller.
 */
public record SendChatMessageRequest(
        @NotBlank @Size(max = 4000) String body
) {
}
