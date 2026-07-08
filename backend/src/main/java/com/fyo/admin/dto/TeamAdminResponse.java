package com.fyo.admin.dto;

import com.fyo.domain.Team;
import java.time.OffsetDateTime;

public record TeamAdminResponse(
        Long id,
        String name,
        String sportName,
        String region,
        String captainUsername,
        short maxPlayers,
        short openSpots,
        boolean recruiting,
        boolean archived,
        OffsetDateTime createdAt
) {
    public static TeamAdminResponse from(Team team) {
        return new TeamAdminResponse(
                team.getId(),
                team.getName(),
                team.getSport().getSportName(),
                team.getRegion(),
                team.getCaptain().getUsername(),
                team.getMaxPlayers(),
                team.getOpenSpots(),
                team.isRecruiting(),
                team.isArchived(),
                team.getCreatedAt()
        );
    }
}