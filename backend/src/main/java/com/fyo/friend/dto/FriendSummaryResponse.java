package com.fyo.friend.dto;

import com.fyo.team.dto.UserSummaryResponse;

public record FriendSummaryResponse(
        Long requestId,
        UserSummaryResponse user,
        java.time.OffsetDateTime friendsSince
) {
}
