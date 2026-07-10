package com.fyo.repository;

import com.fyo.domain.Conversation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByMatchId(Long matchId);

    Optional<Conversation> findByTeamId(Long teamId);

    /** The existing DIRECT thread between two users, if any (they always have exactly two participants). */
    @Query("""
            select c from Conversation c
            where c.type = com.fyo.domain.ConversationType.DIRECT
              and exists (select 1 from ConversationParticipant p
                          where p.conversation = c and p.user.id = :userAId)
              and exists (select 1 from ConversationParticipant p
                          where p.conversation = c and p.user.id = :userBId)
            """)
    Optional<Conversation> findDirectBetween(@Param("userAId") Long userAId, @Param("userBId") Long userBId);
}
