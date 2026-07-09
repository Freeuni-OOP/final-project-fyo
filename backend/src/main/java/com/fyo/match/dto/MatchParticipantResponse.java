package com.fyo.match.dto;

/**
 * One side of a match. For ONE_VS_ONE, userId is set and teamId is null.
 * For TEAM_VS_TEAM, teamId is set and userId is null. Frontend can render
 * this the same way regardless of format - it never has to branch on it.
 */
public record MatchParticipantResponse(
        Long userId,
        Long teamId,
        String displayName,
        String imageUrl
) {
}