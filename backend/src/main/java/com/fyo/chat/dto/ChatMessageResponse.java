package com.fyo.chat.dto;

import java.time.OffsetDateTime;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderUsername,
        String body,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
}
