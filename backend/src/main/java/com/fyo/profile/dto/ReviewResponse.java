package com.fyo.profile.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long matchId,
        Long reviewerUserId,
        String reviewerUsername,
        short score,
        String comment,
        Instant createdAt
) {
}
