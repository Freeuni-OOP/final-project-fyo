package com.fyo.team.dto;

import jakarta.validation.constraints.NotNull;

public record JoinTeamRequest(@NotNull Long userId) {
}
