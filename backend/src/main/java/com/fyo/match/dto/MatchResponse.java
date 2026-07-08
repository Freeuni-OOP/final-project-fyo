package com.fyo.match.dto;

import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchStatus;
import com.fyo.team.dto.SportResponse;
import java.time.OffsetDateTime;

/**
 * API view of a confirmed match. {@code home} / {@code away} are format-agnostic
 * participants (user for ONE_VS_ONE, team for TEAM_VS_TEAM).
 */
public record MatchResponse(
        Long id,
        SportResponse sport,
        MatchFormat format,
        MatchParticipantResponse home,
        MatchParticipantResponse away,
        String location,
        OffsetDateTime proposedDatetime,
        MatchStatus status,
        OffsetDateTime createdAt
) {
}
