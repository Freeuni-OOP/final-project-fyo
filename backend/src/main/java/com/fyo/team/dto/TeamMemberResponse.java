package com.fyo.team.dto;

import com.fyo.domain.TeamMemberRole;
import java.time.OffsetDateTime;

public record TeamMemberResponse(
        Long id,
        Long userId,
        String username,
        String fullName,
        String imageUrl,
        TeamMemberRole role,
        OffsetDateTime joinedAt
) {
}
