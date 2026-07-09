package com.fyo.onboarding.dto;

import java.util.List;

public record OnboardingResponse(
        Long id,
        String name,
        String surname,
        String username,
        int age,
        String sex,
        String region,
        String imageUrl,
        boolean onboardingCompleted,
        List<UserSportResponse> sports
) {
}