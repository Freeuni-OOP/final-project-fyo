package com.fyo.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileSportUpdateDto(
        @NotNull Long sportId,
        @NotBlank String skillLevel
) {
}
