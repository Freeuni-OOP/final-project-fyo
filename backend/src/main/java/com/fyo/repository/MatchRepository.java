package com.fyo.repository;

import com.fyo.domain.Match;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByHomeUserIdOrAwayUserId(Long homeUserId, Long awayUserId);

    List<Match> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);

    @Query("""
            select m from Match m
            where m.homeUser.id = :userId or m.awayUser.id = :userId
            order by m.proposedDatetime desc nulls last, m.id desc
            """)
    List<Match> findHistoryForUser(@Param("userId") Long userId);
}
