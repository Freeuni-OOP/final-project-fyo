package com.fyo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    private String region;

    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "captain_id", nullable = false)
    private User captain;

    @Column(name = "max_players", nullable = false)
    private short maxPlayers;

    @Column(name = "open_spots", nullable = false)
    private short openSpots;

    @Column(name = "is_recruiting", nullable = false)
    private boolean recruiting;

    @Column(name = "is_archived", nullable = false)
    private boolean archived;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Team() {
    }

    public Team(
            String name,
            Sport sport,
            String region,
            String description,
            String logoUrl,
            User captain,
            short maxPlayers,
            short openSpots,
            boolean recruiting
    ) {
        this.name = name;
        this.sport = sport;
        this.region = region;
        this.description = description;
        this.logoUrl = logoUrl;
        this.captain = captain;
        this.maxPlayers = maxPlayers;
        this.openSpots = openSpots;
        this.recruiting = recruiting;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Sport getSport() {
        return sport;
    }

    public String getRegion() {
        return region;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public User getCaptain() {
        return captain;
    }

    public short getMaxPlayers() {
        return maxPlayers;
    }

    public short getOpenSpots() {
        return openSpots;
    }

    public boolean isRecruiting() {
        return recruiting;
    }

    public boolean isArchived() {
        return archived;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void takeOpenSpot() {
        if (openSpots <= 0) {
            throw new IllegalStateException("Team has no open spots");
        }
        openSpots--;
    }
}
