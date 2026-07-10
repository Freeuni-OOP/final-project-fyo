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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "match_requests")
public class MatchRequest {

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
    @JoinColumn(name = "requester_user_id")
    private User requesterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_team_id")
    private Team requesterTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_user_id")
    private User opponentUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_team_id")
    private Team opponentTeam;

    private String location;

    @Column(name = "proposed_datetime")
    private OffsetDateTime proposedDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchRequestStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected MatchRequest() {
    }

    private MatchRequest(
            Sport sport,
            MatchFormat format,
            User requesterUser,
            Team requesterTeam,
            User opponentUser,
            Team opponentTeam,
            String location,
            OffsetDateTime proposedDatetime
    ) {
        this.sport = sport;
        this.format = format;
        this.requesterUser = requesterUser;
        this.requesterTeam = requesterTeam;
        this.opponentUser = opponentUser;
        this.opponentTeam = opponentTeam;
        this.location = location;
        this.proposedDatetime = proposedDatetime;
        this.status = MatchRequestStatus.PENDING;
    }

    public static MatchRequest oneVsOne(
            Sport sport,
            User requesterUser,
            User opponentUser,
            String location,
            OffsetDateTime proposedDatetime
    ) {
        return new MatchRequest(
                sport,
                MatchFormat.ONE_VS_ONE,
                requesterUser,
                null,
                opponentUser,
                null,
                location,
                proposedDatetime
        );
    }

    public static MatchRequest teamVsTeam(
            Sport sport,
            Team requesterTeam,
            Team opponentTeam,
            String location,
            OffsetDateTime proposedDatetime
    ) {
        return new MatchRequest(
                sport,
                MatchFormat.TEAM_VS_TEAM,
                null,
                requesterTeam,
                null,
                opponentTeam,
                location,
                proposedDatetime
        );
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

    public User getRequesterUser() {
        return requesterUser;
    }

    public Team getRequesterTeam() {
        return requesterTeam;
    }

    public User getOpponentUser() {
        return opponentUser;
    }

    public Team getOpponentTeam() {
        return opponentTeam;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getProposedDatetime() {
        return proposedDatetime;
    }

    public MatchRequestStatus getStatus() {
        return status;
    }

    public Match getMatch() {
        return match;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void accept(Match match) {
        if (status != MatchRequestStatus.PENDING) {
            throw new IllegalStateException("Only a pending match request can be accepted");
        }
        this.status = MatchRequestStatus.ACCEPTED;
        this.match = match;
    }

    public void decline() {
        if (status != MatchRequestStatus.PENDING) {
            throw new IllegalStateException("Only a pending match request can be declined");
        }
        this.status = MatchRequestStatus.DECLINED;
    }
}
