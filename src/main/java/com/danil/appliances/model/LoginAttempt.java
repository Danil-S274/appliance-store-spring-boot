package com.danil.appliances.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "login_attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "ip"}))
@Getter
@Setter
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false)
    private int fails;

    private Instant lockedUntil;

    @Column(nullable = false)
    private Instant lastAttempt;
}

