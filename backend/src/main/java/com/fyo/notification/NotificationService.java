package com.fyo.notification;

import com.fyo.domain.User;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationService {

    private static final long STREAM_TIMEOUT_MS = 30L * 60L * 1000L;

    private final AtomicLong ids = new AtomicLong();
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter connect(User user) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        Long userId = user.getId();
        emittersByUser.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(error -> remove(userId, emitter));

        sendToEmitter(userId, emitter, new NotificationEvent(
                ids.incrementAndGet(),
                userId,
                "CONNECTED",
                "Notifications connected",
                null,
                Instant.now()
        ));
        return emitter;
    }

    public void notifyUser(Long userId, String type, String message, String link) {
        if (userId == null) return;
        NotificationEvent event = new NotificationEvent(
                ids.incrementAndGet(),
                userId,
                type,
                message,
                link,
                Instant.now()
        );
        for (SseEmitter emitter : emittersByUser.getOrDefault(userId, new CopyOnWriteArrayList<>())) {
            sendToEmitter(userId, emitter, event);
        }
    }

    public void notifyUsers(List<Long> userIds, String type, String message, String link) {
        userIds.stream().distinct().forEach(userId -> notifyUser(userId, type, message, link));
    }

    public void notifyConnectedUsersExcept(Long excludedUserId, String type, String message, String link) {
        emittersByUser.keySet().stream()
                .filter(userId -> excludedUserId == null || !userId.equals(excludedUserId))
                .forEach(userId -> notifyUser(userId, type, message, link));
    }

    private void sendToEmitter(Long userId, SseEmitter emitter, NotificationEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .id(String.valueOf(event.id()))
                    .data(event));
        } catch (IOException | IllegalStateException e) {
            remove(userId, emitter);
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId);
        }
    }
}
