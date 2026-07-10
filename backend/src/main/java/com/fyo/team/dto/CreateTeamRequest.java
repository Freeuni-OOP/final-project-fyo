package com.fyo.team.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateTeamRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull Long sportId,
        @Size(max = 255) String region,
        String description,
        String logoUrl,
        @NotNull @Min(1) @Max(100) Integer maxPlayers,
        Boolean isRecruiting,
        /** Players the captain adds up front. The captain's own id is ignored. */
        List<Long> memberUserIds
) {
}
