package com.fyo.chat.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ConversationResponse(
        Long id,
        Long matchId,
        List<ConversationParticipantResponse> participants,
        ChatMessageResponse lastMessage,
        OffsetDateTime createdAt
) {
}
