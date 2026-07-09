package com.fyo.profile.dto;

import java.util.List;

public record ProfileResponse(
        Long id,
        String username,
        String name,
        String surname,
        Short age,
        String sex,
        String region,
        String imageUrl,
        String email,
        Double ratingAverage,
        List<ProfileSportResponse> sports,
        List<MatchHistoryItemResponse> matchHistory,
        List<ReviewResponse> reviews
) {
}
