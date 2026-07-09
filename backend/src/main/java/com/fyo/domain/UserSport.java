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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "user_sports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_sports_user_sport",
                columnNames = {"user_id", "sport_id"}
        )
)

public class UserSport{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 30)
    private SkillLevel skillLevel;

    protected UserSport() {
    }

    public UserSport(User user, Sport sport, SkillLevel skillLevel) {
        this.user = user;
        this.sport = sport;
        this.skillLevel = skillLevel;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Sport getSport() {
        return sport;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }
}