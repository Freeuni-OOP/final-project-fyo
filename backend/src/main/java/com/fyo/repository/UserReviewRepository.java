package com.fyo.repository;

import com.fyo.domain.UserReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

    @Query("""
            select r from UserReview r
            left join fetch r.reviewerUser
            left join fetch r.match
            where r.reviewedUser.id = :userId
            order by r.createdAt desc
            """)
    List<UserReview> findByReviewedUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("select avg(r.score) from UserReview r where r.reviewedUser.id = :userId")
    Optional<Double> findAverageScoreByReviewedUserId(@Param("userId") Long userId);
}
