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
                @Index(name = "idx_verification_token_user", columnList = "user_id"),
                @Index(name = "idx_verification_token_expiry", columnList = "expiry_date"),
                @Index(name = "idx_verification_token_used", columnList = "used"),
                @Index(name = "idx_verification_token_type", columnList = "tokenType")
        }
)
public class VerificationToken extends BaseEntity {

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    private boolean used;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenType tokenType;

    @Column
    private String additionalData;
}
