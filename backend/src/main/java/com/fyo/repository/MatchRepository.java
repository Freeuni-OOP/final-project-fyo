package com.fyo.repository;

import com.fyo.domain.Match;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
            select m from Match m
            left join fetch m.sport
            left join fetch m.homeUser
            left join fetch m.awayUser
            left join fetch m.homeTeam
            left join fetch m.awayTeam
            left join fetch m.result
            where m.homeUser.id = :userId or m.awayUser.id = :userId
            order by m.proposedDatetime desc nulls last, m.createdAt desc
            """)
    List<Match> findHistoryForUser(@Param("userId") Long userId);
}
