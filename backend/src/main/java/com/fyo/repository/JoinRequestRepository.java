package com.fyo.repository;

import com.fyo.domain.JoinRequest;
import com.fyo.domain.JoinRequestStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    Optional<JoinRequest> findByTeamIdAndUserId(Long teamId, Long userId);

    List<JoinRequest> findByTeamIdAndStatus(Long teamId, JoinRequestStatus status);

    List<JoinRequest> findByUserIdAndStatusInAndTeamArchivedFalseOrderByCreatedAtDesc(
            Long userId,
            Collection<JoinRequestStatus> statuses
    );

    Optional<JoinRequest> findByIdAndTeamId(Long id, Long teamId);
}
