package com.fyo.repository;

import com.fyo.domain.MatchRequest;
import com.fyo.domain.MatchRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRequestRepository extends JpaRepository<MatchRequest, Long> {

    List<MatchRequest> findByStatus(MatchRequestStatus status);

    List<MatchRequest> findByRequesterUserIdOrOpponentUserId(Long requesterUserId, Long opponentUserId);

    List<MatchRequest> findByRequesterTeamIdOrOpponentTeamId(Long requesterTeamId, Long opponentTeamId);
}
