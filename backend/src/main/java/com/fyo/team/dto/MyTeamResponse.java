package com.fyo.team.dto;

import com.fyo.domain.TeamMemberRole;
import java.time.OffsetDateTime;

/** One row of "teams I'm on": the team, plus how the caller relates to it. */
public record MyTeamResponse(
        TeamSummaryResponse team,
        TeamMemberRole role,
        OffsetDateTime joinedAt
) {
}
