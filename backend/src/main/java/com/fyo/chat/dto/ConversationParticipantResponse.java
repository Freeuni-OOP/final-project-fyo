package com.fyo.chat.dto;

public record ConversationParticipantResponse(
        Long userId,
        String username,
        String fullName,
        String imageUrl
) {
}
