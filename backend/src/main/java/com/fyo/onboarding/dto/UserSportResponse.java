package com.fyo.onboarding.dto;

public record UserSportResponse(
        Long sportId,
        String sportName,
        String skillLevel
) {
}