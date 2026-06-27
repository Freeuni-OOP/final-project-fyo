package com.fyo.team.dto;

public record TeamSummaryResponse(
        Long id,
        String name,
        SportResponse sport,
        String region,
        String description,
        String logoUrl,
        UserSummaryResponse captain,
        int maxPlayers,
        int openSpots,
        boolean isRecruiting
) {
}
