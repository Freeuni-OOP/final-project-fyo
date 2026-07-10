package com.fyo.chat.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Creates (or returns) the 1:1 conversation between the authenticated caller
 * and {@code otherUserId}. Match conversations are created by the listing
 * accept flow, never through this endpoint.
 */
public record CreateDirectConversationRequest(
        @NotNull Long otherUserId
) {
}
