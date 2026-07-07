package com.fyo.repository;

import com.fyo.domain.Match;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByHomeUserIdOrAwayUserId(Long homeUserId, Long awayUserId);

    List<Match> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);
}