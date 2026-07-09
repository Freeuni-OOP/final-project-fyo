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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * A confirmed match, either ONE_VS_ONE (two users) or TEAM_VS_TEAM (two teams).
 * Only ever created via {@link #oneVsOne} / {@link #teamVsTeam} so the
 * participant combination can never be inconsistent with the format - the
 * database enforces the same rule via chk_matches_participants as a backstop.
 */
@Entity
@Table(name = "matches")
public class Match {

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
    @JoinColumn(name = "home_user_id")
    private User homeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_user_id")
    private User awayUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private String location;

    @Column(name = "proposed_datetime")
    private OffsetDateTime proposedDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MatchResult result;

    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MatchListing listing;

    protected Match() {
    }

    private Match(
            Sport sport,
            MatchFormat format,
            User homeUser,
            User awayUser,
            Team homeTeam,
            Team awayTeam,
            String location,
            OffsetDateTime proposedDatetime
    ) {
        this.sport = sport;
        this.format = format;
        this.homeUser = homeUser;
        this.awayUser = awayUser;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.location = location;
        this.proposedDatetime = proposedDatetime;
        this.status = MatchStatus.UPCOMING;
    }

    public static Match oneVsOne(Sport sport, User homeUser, User awayUser, String location, OffsetDateTime proposedDatetime) {
        return new Match(sport, MatchFormat.ONE_VS_ONE, homeUser, awayUser, null, null, location, proposedDatetime);
    }

    public static Match teamVsTeam(Sport sport, Team homeTeam, Team awayTeam, String location, OffsetDateTime proposedDatetime) {
        return new Match(sport, MatchFormat.TEAM_VS_TEAM, null, null, homeTeam, awayTeam, location, proposedDatetime);
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

    public User getHomeUser() {
        return homeUser;
    }

    public User getAwayUser() {
        return awayUser;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getProposedDatetime() {
        return proposedDatetime;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public MatchResult getResult() {
        return result;
    }

    public MatchListing getListing() {
        return listing;
    }

    void linkResult(MatchResult result) {
        this.result = result;
    }

    void linkListing(MatchListing listing) {
        this.listing = listing;
    }

    public boolean hasParticipant(Long userId, Long teamId) {
        if (format == MatchFormat.ONE_VS_ONE) {
            return userId != null && (userId.equals(idOf(homeUser)) || userId.equals(idOf(awayUser)));
        }
        return teamId != null && (teamId.equals(idOf(homeTeam)) || teamId.equals(idOf(awayTeam)));
    }

    public void cancel() {
        if (status != MatchStatus.UPCOMING) {
            throw new IllegalStateException("Only an upcoming match can be cancelled");
        }
        status = MatchStatus.CANCELLED;
    }

    public void complete() {
        if (status != MatchStatus.UPCOMING) {
            throw new IllegalStateException("Only an upcoming match can be completed");
        }
        status = MatchStatus.COMPLETED;
    }

    private static Long idOf(User user) {
        return user == null ? null : user.getId();
    }

    private static Long idOf(Team team) {
        return team == null ? null : team.getId();
    }
}
