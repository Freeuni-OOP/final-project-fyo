package com.fyo.match.dto;

import com.fyo.domain.MatchFormat;
import com.fyo.domain.MatchListingStatus;
import java.time.OffsetDateTime;

public record MatchListingItem(
        Long id,
        SportResponse sport,
        MatchFormat format,
        MatchParticipantResponse postedBy,
        String location,
        OffsetDateTime proposedDatetime,
        MatchListingStatus status,
        Long matchId,
        OffsetDateTime createdAt
) {
}
