package com.fyo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
        @NotNull Long senderUserId,
        @NotBlank @Size(max = 4000) String body
) {
}
