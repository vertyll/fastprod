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
                @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_token_expiry_date", columnList = "expiry_date"),
                @Index(name = "idx_refresh_token_is_revoked", columnList = "is_revoked"),
                @Index(name = "idx_refresh_token_token", columnList = "token"),
        }
)
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_token_user")
    )
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean isRevoked = false;

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
