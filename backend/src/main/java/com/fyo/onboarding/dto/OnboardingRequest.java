package com.fyo.onboarding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OnboardingRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String surname,
        @NotBlank @Size(max = 255) String username,
        @NotNull @Min(10) @Max(100) Integer age,
        @NotBlank String sex,
        @Size(max = 255) String region,
        String imageUrl,
        @NotNull @Size(min = 1) List<@Valid UserSportDto> sports
) {
}