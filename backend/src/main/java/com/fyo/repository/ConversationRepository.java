package com.fyo.repository;

import com.fyo.domain.Conversation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByMatchId(Long matchId);
}
