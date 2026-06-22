package com.fyo.repository;

import com.fyo.domain.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByArchivedFalse();

    Optional<Team> findByIdAndArchivedFalse(Long id);
}
