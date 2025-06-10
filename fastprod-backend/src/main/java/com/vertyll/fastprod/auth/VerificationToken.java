package com.vertyll.fastprod.auth;

import com.vertyll.fastprod.common.entity.BaseEntity;
import com.vertyll.fastprod.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "verification_token")
public class VerificationToken extends BaseEntity {

    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    private boolean used;
}