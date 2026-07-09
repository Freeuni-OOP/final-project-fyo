package com.fyo.domain;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A user or team posting that they need an opponent. Others can respond via
 * {@link MatchListingResponse}; when the poster accepts one, the listing is
 * {@link #fill(Match)}-ed with the resulting Match and every other pending
 * response is expected to be auto-declined by the service layer.
 */
@Entity
@Table(name = "match_listings")
public class MatchListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchFormat format;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_user_id")
    private User postedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_team_id")
    private Team postedByTeam;

    private String location;

    @Column(name = "proposed_datetime")
    private OffsetDateTime proposedDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchListingStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchListingResponse> responses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected MatchListing() {
    }

    private MatchListing(Sport sport, MatchFormat format, User postedByUser, Team postedByTeam, String location, OffsetDateTime proposedDatetime) {
        this.sport = sport;
        this.format = format;
        this.postedByUser = postedByUser;
        this.postedByTeam = postedByTeam;
        this.location = location;
        this.proposedDatetime = proposedDatetime;
        this.status = MatchListingStatus.OPEN;
    }

    public static MatchListing postedByUser(Sport sport, User postedByUser, String location, OffsetDateTime proposedDatetime) {
        return new MatchListing(sport, MatchFormat.ONE_VS_ONE, postedByUser, null, location, proposedDatetime);
    }

    public static MatchListing postedByTeam(Sport sport, Team postedByTeam, String location, OffsetDateTime proposedDatetime) {
        return new MatchListing(sport, MatchFormat.TEAM_VS_TEAM, null, postedByTeam, location, proposedDatetime);
    }

    public Long getId() {
        return id;
    }

    public Sport getSport() {
        return sport;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public User getPostedByUser() {
        return postedByUser;
    }

    public Team getPostedByTeam() {
        return postedByTeam;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getProposedDatetime() {
        return proposedDatetime;
    }

    public MatchListingStatus getStatus() {
        return status;
    }

    public Match getMatch() {
        return match;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    void addResponse(MatchListingResponse response) {
        responses.add(response);
    }

    public boolean isPostedBy(Long userId, Long teamId) {
        if (format == MatchFormat.ONE_VS_ONE) {
            return userId != null && postedByUser != null && userId.equals(postedByUser.getId());
        }
        return teamId != null && postedByTeam != null && teamId.equals(postedByTeam.getId());
    }

    public void fill(Match match) {
        if (status != MatchListingStatus.OPEN) {
            throw new IllegalStateException("Only an open listing can be filled");
        }
        this.status = MatchListingStatus.FILLED;
        this.match = match;
        match.linkListing(this);
    }

    public void cancel() {
        if (status != MatchListingStatus.OPEN) {
            throw new IllegalStateException("Only an open listing can be cancelled");
        }
        this.status = MatchListingStatus.CANCELLED;
    }
}