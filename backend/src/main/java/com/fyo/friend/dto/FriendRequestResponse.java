package com.fyo.friend.dto;

import com.fyo.domain.FriendRequestStatus;
import com.fyo.team.dto.UserSummaryResponse;
import java.time.OffsetDateTime;

public record FriendRequestResponse(
        Long id,
        UserSummaryResponse requester,
        UserSummaryResponse addressee,
        FriendRequestStatus status,
        OffsetDateTime createdAt
) {
}
