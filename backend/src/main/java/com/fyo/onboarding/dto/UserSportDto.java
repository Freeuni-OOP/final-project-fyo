package com.fyo.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSportDto(
        @NotNull Long sportId,
        @NotBlank String skillLevel
) {
}