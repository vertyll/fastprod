package com.vertyll.fastprod.auth.entity;

import java.io.Serial;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.sharedinfrastructure.entity.BaseEntity;
import com.vertyll.fastprod.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "refresh_token",
    indexes = {
        @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_token_expiry_date", columnList = "expiry_date"),
        @Index(name = "idx_refresh_token_is_revoked", columnList = "is_revoked"),
        @Index(name = "idx_refresh_token_token", columnList = "token"),
    }
)
public class RefreshToken extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_user"))
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    private @Nullable String deviceInfo;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column
    private Instant lastUsedAt;

    @Column
    private @Nullable Instant revokedAt;

    @SuppressWarnings({"NullAway.Init", "java:S2637"})
    protected RefreshToken() {
        super();
    }

    @Builder
    private RefreshToken(
        String token,
        User user,
        Instant expiryDate,
        @Nullable String deviceInfo,
        String ipAddress,
        String userAgent,
        Instant lastUsedAt
    ) {
        super();
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.lastUsedAt = lastUsedAt;
    }

    public void revoke(Instant at) {
        this.revoked = true;
        this.revokedAt = at;
    }

    public void markUsed(Instant at) {
        this.lastUsedAt = at;
    }
}
