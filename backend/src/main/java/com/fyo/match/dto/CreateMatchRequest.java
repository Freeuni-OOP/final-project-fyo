package com.fyo.match.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CreateMatchRequest(
        @NotNull Long sportId,
        Long requesterUserId,
        Long requesterTeamId,
        Long opponentUserId,
        Long opponentTeamId,
        @Size(max = 255) String location,
        OffsetDateTime proposedDatetime,
        @NotNull Long actingUserId
) {
}
