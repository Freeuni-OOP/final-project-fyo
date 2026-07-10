package com.fyo.friend.dto;

public record FriendStatusResponse(
        Long userId,
        FriendRelationshipStatus status,
        Long requestId
) {
}
