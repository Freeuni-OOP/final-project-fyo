package com.fyo.team.dto;

public record UserSummaryResponse(
        Long id,
        String username,
        String name,
        String surname,
        String region,
        String imageUrl
) {
}
