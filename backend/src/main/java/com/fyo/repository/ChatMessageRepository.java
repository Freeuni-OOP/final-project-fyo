package com.fyo.repository;

import com.fyo.domain.ChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Optional<ChatMessage> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
