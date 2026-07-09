package com.fyo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(nullable = false, length = 30)
    private String format;

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
    private Instant proposedDatetime;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "match", fetch = FetchType.LAZY)
    private MatchResult result;

    protected Match() {
    }

    public Long getId() {
        return id;
    }

    public Sport getSport() {
        return sport;
    }

    public String getFormat() {
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

    public Instant getProposedDatetime() {
        return proposedDatetime;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MatchResult getResult() {
        return result;
    }
}
