package com.fyo.admin.dto;

import com.fyo.domain.Sport;

public record SportAdminResponse(
        Long id,
        String sportName
) {
    public static SportAdminResponse from(Sport sport) {
        return new SportAdminResponse(
                sport.getId(),
                sport.getSportName()
        );
    }
}