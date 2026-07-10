package com.fyo.match.dto;

/**
 * Outcome of accepting a listing response: the confirmed match and the
 * conversation auto-created for its participants.
 */
public record AcceptListingResult(
        Long listingId,
        Long matchId,
        Long conversationId
) {
}
