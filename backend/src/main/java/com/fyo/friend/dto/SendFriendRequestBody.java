package com.fyo.friend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SendFriendRequestBody(
        @NotNull @Positive Long addresseeUserId
) {
}
