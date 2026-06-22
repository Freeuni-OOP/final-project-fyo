package com.fyo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sports")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sport_name", nullable = false, unique = true)
    private String sportName;

    protected Sport() {
    }

    public Long getId() {
        return id;
    }

    public String getSportName() {
        return sportName;
    }
}
