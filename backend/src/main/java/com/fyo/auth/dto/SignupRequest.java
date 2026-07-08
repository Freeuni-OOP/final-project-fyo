package com.fyo.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional extra profile fields for signup. Identity (uid, email) always comes
 * from the verified Firebase ID token, never from this body.
 */
public record SignupRequest(
        @Size(max = 255) String name,
        @Size(max = 255) String surname,
        @Size(max = 255) String username
) {

    public static SignupRequest empty() {
        return new SignupRequest(null, null, null);
    }
}
