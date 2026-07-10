package com.fyo.match.dto;

import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchRequestStatus;
import java.time.OffsetDateTime;

public record MatchRequestResponse(
        Long id,
        SportResponse sport,
        MatchFormat format,
        MatchParticipantResponse requester,
        MatchParticipantResponse opponent,
        String location,
        OffsetDateTime proposedDatetime,
        MatchRequestStatus status,
        Long matchId,
        OffsetDateTime createdAt
) {
}
