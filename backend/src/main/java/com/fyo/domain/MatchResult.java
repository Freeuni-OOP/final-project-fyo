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
@Table(name = "match_results")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Column(name = "home_score")
    private Short homeScore;

    @Column(name = "away_score")
    private Short awayScore;

    @Column(length = 30)
    private String winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedByUser;

    @Column(name = "confirmation_status", nullable = false, length = 30)
    private String confirmationStatus;

    @Column(name = "submitted_at", nullable = false, insertable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    protected MatchResult() {
    }

    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public Short getHomeScore() {
        return homeScore;
    }

    public Short getAwayScore() {
        return awayScore;
    }

    public String getWinner() {
        return winner;
    }

    public User getSubmittedByUser() {
        return submittedByUser;
    }

    public String getConfirmationStatus() {
        return confirmationStatus;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }
}
