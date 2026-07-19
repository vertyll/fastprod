package com.vertyll.fastprod.auth.entity;

import java.io.Serial;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.vertyll.fastprod.auth.enums.VerificationTokenType;
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

    @Serial
    private static final long serialVersionUID = 1L;

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_verification_token_user"))
    private User user;

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationTokenType tokenType;

    @Column
    private String additionalData;
}
