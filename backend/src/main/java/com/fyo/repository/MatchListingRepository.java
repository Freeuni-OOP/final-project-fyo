package com.fyo.repository;

import com.fyo.domain.MatchListing;
import com.fyo.domain.MatchListingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchListingRepository extends JpaRepository<MatchListing, Long> {

    List<MatchListing> findByStatus(MatchListingStatus status);

    List<MatchListing> findBySportIdAndStatus(Long sportId, MatchListingStatus status);
}