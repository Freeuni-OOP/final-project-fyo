package com.fyo.repository;

import com.fyo.domain.MatchListingResponse;
import com.fyo.domain.MatchListingResponseStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchListingResponseRepository extends JpaRepository<MatchListingResponse, Long> {

    List<MatchListingResponse> findByListingIdAndStatus(Long listingId, MatchListingResponseStatus status);

    Optional<MatchListingResponse> findByIdAndListingId(Long id, Long listingId);

    boolean existsByListingIdAndResponderUserId(Long listingId, Long responderUserId);

    boolean existsByListingIdAndResponderTeamId(Long listingId, Long responderTeamId);
}