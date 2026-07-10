package com.fyo.team.dto;

import com.fyo.domain.JoinRequestStatus;
import java.time.OffsetDateTime;

/**
 * A join request seen from the applicant's side, so it names the team rather
 * than the user. {@link JoinRequestResponse} is the captain's mirror of this.
 */
public record MyJoinRequestResponse(
        Long id,
        TeamSummaryResponse team,
        JoinRequestStatus status,
        OffsetDateTime createdAt
) {
}
