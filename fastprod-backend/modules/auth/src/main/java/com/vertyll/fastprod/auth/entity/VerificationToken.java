package com.vertyll.fastprod.auth.entity;

import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.common.entity.BaseEntity;
import com.vertyll.fastprod.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "verification_token",
        indexes = {
                @Index(name = "idx_verification_token_user_id", columnList = "user_id"),
                @Index(name = "idx_verification_token_expiry_date", columnList = "expiry_date"),
                @Index(name = "idx_verification_token_is_used", columnList = "is_used"),
                @Index(name = "idx_verification_token_token_type", columnList = "token_type"),
                @Index(name = "idx_verification_token_token", columnList = "token")
        }
)
public class VerificationToken extends BaseEntity {

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_verification_token_user")
    )
    private User user;

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean isUsed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenType tokenType;

    @Column
    private String additionalData;
}
