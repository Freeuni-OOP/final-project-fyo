package com.fyo.repository;

import com.fyo.domain.MatchResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    Optional<MatchResult> findByMatchId(Long matchId);
}