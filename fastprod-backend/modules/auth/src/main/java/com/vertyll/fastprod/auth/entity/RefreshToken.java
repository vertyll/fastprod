package com.vertyll.fastprod.auth.entity;

import com.vertyll.fastprod.common.entity.BaseEntity;
import com.vertyll.fastprod.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token_user", columnList = "user_id"),
                @Index(name = "idx_refresh_token_expiry", columnList = "expiry_date"),
                @Index(name = "idx_refresh_token_revoked", columnList = "revoked")
        }
)
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(length = 255)
    private String deviceInfo;

    @Column(length = 45) // IPv6 max length
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column
    private Instant lastUsedAt;

    @Column
    private Instant revokedAt;
}