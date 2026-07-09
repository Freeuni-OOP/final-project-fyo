package com.fyo.admin.dto;

import com.fyo.domain.User;
import java.time.Instant;

public record UserAdminResponse(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        String region,
        String imageUrl,
        boolean admin,
        boolean archived,
        Instant createdAt,
        Instant archivedAt
) {
    public static UserAdminResponse from(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getRegion(),
                user.getImageUrl(),
                user.isAdmin(),
                user.isArchived(),
                user.getCreatedAt(),
                user.getArchivedAt()
        );
    }
}