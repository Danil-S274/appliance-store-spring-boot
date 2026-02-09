package com.danil.appliances.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_user", columnList = "user_email"),
                @Index(name = "idx_refresh_jti_hash", columnList = "jti_hash", unique = true)
        })
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_email", nullable=false, length=150)
    private String userEmail;

    @Column(name="jti_hash", nullable=false, length=64, unique=true)
    private String jtiHash;

    @Column(name="expires_at", nullable=false)
    private Instant expiresAt;

    @Column(name="revoked_at")
    private Instant revokedAt;

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
