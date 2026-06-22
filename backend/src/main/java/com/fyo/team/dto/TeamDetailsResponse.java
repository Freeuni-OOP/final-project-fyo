package com.fyo.team.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record TeamDetailsResponse(
        Long id,
        String name,
        SportResponse sport,
        String region,
        String description,
        String logoUrl,
        UserSummaryResponse captain,
        int maxPlayers,
        int openSpots,
        boolean isRecruiting,
        OffsetDateTime createdAt,
        List<TeamMemberResponse> members
) {
}
