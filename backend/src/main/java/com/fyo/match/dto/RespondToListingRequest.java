package com.fyo.match.dto;

/**
 * Volunteers to fill a listing. For ONE_VS_ONE listings the responder is the
 * authenticated caller and {@code teamId} must be null; for TEAM_VS_TEAM it
 * names the responding team, whose captain the caller must be.
 */
public record RespondToListingRequest(
        Long teamId
) {
}
