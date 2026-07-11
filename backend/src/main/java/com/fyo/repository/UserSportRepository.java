package com.fyo.repository;

import com.fyo.domain.SkillLevel;
import com.fyo.domain.UserSport;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSportRepository extends JpaRepository<UserSport, Long> {
    List<UserSport> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    @Query("""
            SELECT us FROM UserSport us
            JOIN FETCH us.user u
            JOIN FETCH us.sport s
            WHERE u.archived = false
              AND (:sportId IS NULL OR s.id = :sportId)
              AND (:skillLevel IS NULL OR us.skillLevel = :skillLevel)
            ORDER BY u.username
            """)
    List<UserSport> search(
            @Param("sportId") Long sportId,
            @Param("skillLevel") SkillLevel skillLevel,
            Pageable pageable);

    @Query("""
            SELECT us FROM UserSport us
            JOIN FETCH us.user u
            JOIN FETCH us.sport s
            WHERE u.archived = false
              AND (:sportId IS NULL OR s.id = :sportId)
              AND LOWER(u.region) = LOWER(:region)
              AND (:skillLevel IS NULL OR us.skillLevel = :skillLevel)
            ORDER BY u.username
            """)
    List<UserSport> searchByRegion(
            @Param("sportId") Long sportId,
            @Param("region") String region,
            @Param("skillLevel") SkillLevel skillLevel,
            Pageable pageable);
}
