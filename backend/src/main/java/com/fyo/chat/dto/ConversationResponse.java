package com.fyo.chat.dto;

import com.fyo.domain.ConversationType;
import java.time.OffsetDateTime;
import java.util.List;

public record ConversationResponse(
        Long id,
        ConversationType type,
        Long matchId,
        Long teamId,
        String title,
        String subtitle,
        List<ConversationParticipantResponse> participants,
        ChatMessageResponse lastMessage,
        OffsetDateTime createdAt
) {
}

