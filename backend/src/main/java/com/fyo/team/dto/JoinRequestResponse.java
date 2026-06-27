package com.fyo.team.dto;

import com.fyo.domain.JoinRequestStatus;
import java.time.OffsetDateTime;

public record JoinRequestResponse(
        Long id,
        Long teamId,
        Long userId,
        String username,
        String fullName,
        String imageUrl,
        JoinRequestStatus status,
        OffsetDateTime createdAt
) {
}
