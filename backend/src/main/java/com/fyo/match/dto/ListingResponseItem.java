package com.fyo.match.dto;

import com.fyo.domain.MatchListingResponseStatus;
import java.time.OffsetDateTime;

public record ListingResponseItem(
        Long id,
        Long listingId,
        MatchParticipantResponse responder,
        MatchListingResponseStatus status,
        OffsetDateTime createdAt
) {
}
