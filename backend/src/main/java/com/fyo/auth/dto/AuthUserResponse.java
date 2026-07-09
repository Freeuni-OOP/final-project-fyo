package com.fyo.auth.dto;

import com.fyo.domain.User;

public record AuthUserResponse(
        Long id,
        String firebaseUid,
        String username,
        String email,
        String name,
        String surname,
        String imageUrl,
        boolean onboarding
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getFirebaseUid(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getImageUrl(),
                user.isOnboarding()
        );
    }
}
