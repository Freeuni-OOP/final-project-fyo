package com.fyo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * A user or team volunteering to fill a {@link MatchListing}. Note this
 * intentionally has no relation to Task 5's targeted match-request feature -
 * a listing response is only ever created against an OPEN listing that has
 * no specific opponent picked yet.
 */
@Entity
@Table(name = "match_listing_responses")
public class MatchListingResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private MatchListing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_user_id")
    private User responderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_team_id")
    private Team responderTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchListingResponseStatus status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected MatchListingResponse() {
    }

    private MatchListingResponse(MatchListing listing, User responderUser, Team responderTeam) {
        this.listing = listing;
        this.responderUser = responderUser;
        this.responderTeam = responderTeam;
        this.status = MatchListingResponseStatus.PENDING;
        listing.addResponse(this);
    }

    public static MatchListingResponse fromUser(MatchListing listing, User responderUser) {
        return new MatchListingResponse(listing, responderUser, null);
    }

    public static MatchListingResponse fromTeam(MatchListing listing, Team responderTeam) {
        return new MatchListingResponse(listing, null, responderTeam);
    }

    public Long getId() {
        return id;
    }

    public MatchListing getListing() {
        return listing;
    }

    public User getResponderUser() {
        return responderUser;
    }

    public Team getResponderTeam() {
        return responderTeam;
    }

    public MatchListingResponseStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void accept() {
        if (status != MatchListingResponseStatus.PENDING) {
            throw new IllegalStateException("Only a pending response can be accepted");
        }
        status = MatchListingResponseStatus.ACCEPTED;
    }

    public void decline() {
        if (status != MatchListingResponseStatus.PENDING) {
            throw new IllegalStateException("Only a pending response can be declined");
        }
        status = MatchListingResponseStatus.DECLINED;
    }
}