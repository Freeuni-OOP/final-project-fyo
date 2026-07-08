package com.fyo.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSportRequest(
        @NotBlank String sportName
) {
}