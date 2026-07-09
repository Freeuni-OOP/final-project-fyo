package com.fyo.match.dto;

import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchStatus;
import java.time.OffsetDateTime;

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
