package com.fyo.chat.dto;

import jakarta.validation.constraints.NotNull;

public record CreateDirectConversationRequest(
        @NotNull Long userAId,
        @NotNull Long userBId,
        Long matchId
) {
}
