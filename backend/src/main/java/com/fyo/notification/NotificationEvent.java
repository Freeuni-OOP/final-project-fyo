package com.fyo.notification;

import java.time.Instant;

public record NotificationEvent(
        long id,
        Long recipientUserId,
        String type,
        String message,
        String link,
        Instant createdAt
) {
}
