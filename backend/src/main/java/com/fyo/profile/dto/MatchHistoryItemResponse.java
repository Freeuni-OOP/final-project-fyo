package com.fyo.profile.dto;

import java.time.Instant;

public record MatchHistoryItemResponse(
        Long matchId,
        String sportName,
        String format,
        String status,
        String location,
        Instant proposedDatetime,
        Long opponentUserId,
        String opponentUsername,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        Short homeScore,
        Short awayScore,
        String winner
) {
}
