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

/**
 * The result of a completed match, submitted by one side and confirmed or
 * rejected by the other. A match has at most one result row (match_id is
 * unique) - a rejected result is overwritten in place by
 * {@link #resubmit(short, short, User)} rather than creating a new row, so
 * the rejection is not kept as separate history, only the current state.
 */
@Entity
@Table(name = "match_results")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Column(name = "home_score", nullable = false)
    private short homeScore;

    @Column(name = "away_score", nullable = false)
    private short awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchWinner winner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_status", nullable = false)
    private MatchResultConfirmationStatus confirmationStatus;

    @Column(name = "submitted_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    protected MatchResult() {
    }

    public MatchResult(Match match, short homeScore, short awayScore, User submittedByUser) {
        this.match = match;
        this.submittedByUser = submittedByUser;
        applyScore(homeScore, awayScore);
        this.confirmationStatus = MatchResultConfirmationStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public short getHomeScore() {
        return homeScore;
    }

    public short getAwayScore() {
        return awayScore;
    }

    public MatchWinner getWinner() {
        return winner;
    }

    public User getSubmittedByUser() {
        return submittedByUser;
    }

    public MatchResultConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void confirm() {
        if (confirmationStatus != MatchResultConfirmationStatus.PENDING) {
            throw new IllegalStateException("Only a pending result can be confirmed");
        }
        confirmationStatus = MatchResultConfirmationStatus.CONFIRMED;
        confirmedAt = OffsetDateTime.now();
    }

    public void reject() {
        if (confirmationStatus != MatchResultConfirmationStatus.PENDING) {
            throw new IllegalStateException("Only a pending result can be rejected");
        }
        confirmationStatus = MatchResultConfirmationStatus.REJECTED;
    }

    public void resubmit(short homeScore, short awayScore, User submittedByUser) {
        if (confirmationStatus != MatchResultConfirmationStatus.REJECTED) {
            throw new IllegalStateException("Only a rejected result can be resubmitted");
        }
        this.submittedByUser = submittedByUser;
        applyScore(homeScore, awayScore);
        this.confirmationStatus = MatchResultConfirmationStatus.PENDING;
        this.confirmedAt = null;
    }

    private void applyScore(short homeScore, short awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        if (homeScore > awayScore) {
            this.winner = MatchWinner.HOME;
        } else if (awayScore > homeScore) {
            this.winner = MatchWinner.AWAY;
        } else {
            this.winner = MatchWinner.DRAW;
        }
    }
}