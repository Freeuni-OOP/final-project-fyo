package com.fyo.chat.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Creates a 1:1 conversation between the authenticated caller and
 * {@code otherUserId}. The caller's own id comes from the Bearer token.
 */
public record CreateDirectConversationRequest(
        @NotNull Long otherUserId,
        Long matchId
) {
}
