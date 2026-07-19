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

import com.vertyll.fastprod.sharedinfrastructure.entity.BaseEntity;
import com.vertyll.fastprod.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
    @Column(nullable = false)
    private boolean revoked = false;

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
