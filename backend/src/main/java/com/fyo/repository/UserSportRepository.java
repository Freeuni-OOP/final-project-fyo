package com.fyo.repository;

import com.fyo.domain.UserSport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSportRepository extends JpaRepository<UserSport, Long> {
    List<UserSport> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}