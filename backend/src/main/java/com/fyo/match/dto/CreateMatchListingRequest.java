package com.fyo.match.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Posts a "looking for an opponent" listing. When {@code teamId} is null the
 * listing is ONE_VS_ONE posted by the authenticated caller; when set it is
 * TEAM_VS_TEAM posted by that team, and the caller must be its captain.
 */
public record CreateMatchListingRequest(
        @NotNull Long sportId,
        Long teamId,
        String location,
        OffsetDateTime proposedDatetime
) {
}
