package com.fyo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * A chat thread. Created only via the static factories so the type always
 * matches its link columns (the DB enforces the same rule via
 * chk_conversations_type_links).
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @Column(name = "match_id", unique = true)
    private Long matchId;

    @Column(name = "team_id", unique = true)
    private Long teamId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Conversation() {
    }

    private Conversation(ConversationType type, Long matchId, Long teamId) {
        this.type = type;
        this.matchId = matchId;
        this.teamId = teamId;
        this.createdAt = OffsetDateTime.now();
    }

    public static Conversation direct() {
        return new Conversation(ConversationType.DIRECT, null, null);
    }

    public static Conversation forMatch(Long matchId) {
        return new Conversation(ConversationType.MATCH, matchId, null);
    }

    public static Conversation forTeam(Long teamId) {
        return new Conversation(ConversationType.TEAM, null, teamId);
    }

    public Long getId() {
        return id;
    }

    public ConversationType getType() {
        return type;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
