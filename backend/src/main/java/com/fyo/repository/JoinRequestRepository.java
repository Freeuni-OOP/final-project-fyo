package com.fyo.repository;

import com.fyo.domain.JoinRequest;
import com.fyo.domain.JoinRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    List<JoinRequest> findByTeamIdAndStatus(Long teamId, JoinRequestStatus status);

    Optional<JoinRequest> findByIdAndTeamId(Long id, Long teamId);
}