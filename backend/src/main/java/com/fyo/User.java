package com.fyo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Setter
    @Getter
    @Column(name = "firebase_uid", nullable = false, unique = true)
    private String firebaseUid;

    @Setter
    @Getter
    @Column(name = "name", nullable = false)
    private String name;

    @Setter
    @Getter
    @Column(name = "surname", nullable = false)
    private String surname;

    @Setter
    @Getter
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Setter
    @Getter
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Setter
    @Getter
    private Short age;

    public enum Sex {
        MALE, FEMALE, OTHER
    }

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Setter
    @Getter
    private String region;

    @Setter
    @Getter
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Setter
    @Getter
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Setter
    @Getter
    @Column(name = "is_onboarding", nullable = false)
    private boolean isOnboarding = false;

    @Setter
    @Getter
    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Setter
    @Getter
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Setter
    @Getter
    @Column(name = "archived_at")
    private Instant archivedAt;

    protected User() {
    }

    public User(String firebaseUid, String name, String surname, String username, String email) {
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
    }
}
