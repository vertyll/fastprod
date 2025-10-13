package com.vertyll.fastprod.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.vertyll.fastprod.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.expiryDate < :date AND v.isUsed = :isUsed")
    int deleteByExpiryDateBeforeAndIsUsed(LocalDateTime date, boolean isUsed);
}
