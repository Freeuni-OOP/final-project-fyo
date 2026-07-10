package com.fyo.repository;

import com.fyo.domain.FriendRequest;
import com.fyo.domain.FriendRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    @Query("""
            select fr from FriendRequest fr
            where (fr.requester.id = :userAId and fr.addressee.id = :userBId)
               or (fr.requester.id = :userBId and fr.addressee.id = :userAId)
            """)
    List<FriendRequest> findBetweenUsers(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    List<FriendRequest> findByAddresseeIdAndStatusOrderByCreatedAtDesc(
            Long addresseeId, FriendRequestStatus status);

    List<FriendRequest> findByRequesterIdAndStatusOrderByCreatedAtDesc(
            Long requesterId, FriendRequestStatus status);

    @Query("""
            select fr from FriendRequest fr
            where fr.status = com.fyo.domain.FriendRequestStatus.ACCEPTED
              and (fr.requester.id = :userId or fr.addressee.id = :userId)
            order by fr.updatedAt desc
            """)
    List<FriendRequest> findAcceptedForUser(@Param("userId") Long userId);
}
