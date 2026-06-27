package com.fyo.repository;

import com.fyo.domain.TeamMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    List<TeamMember> findByTeamId(Long teamId);
}
