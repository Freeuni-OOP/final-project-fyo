package com.fyo.repository;

import com.fyo.domain.ChatMessage;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    List<ChatMessage> findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long conversationId, OffsetDateTime createdAt, Pageable pageable);

    Optional<ChatMessage> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
