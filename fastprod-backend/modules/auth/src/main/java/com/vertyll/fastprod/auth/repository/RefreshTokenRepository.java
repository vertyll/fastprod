package com.vertyll.fastprod.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vertyll.fastprod.auth.entity.RefreshToken;
import com.vertyll.fastprod.user.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevoked(User user, boolean revoked);

    Optional<RefreshToken> findByUserEmailAndTokenAndRevoked(
            String email, String token, boolean isRevoked);

    @Modifying
    @Query(
            "UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllUserTokens(@Param("user") User user);

    @Modifying
    @Query(
            "DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR (rt.revoked = true AND rt.revokedAt < :thirtyDaysAgo)")
    int deleteAllExpiredTokens(
            @Param("now") Instant now, @Param("thirtyDaysAgo") Instant thirtyDaysAgo);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredTokens(@Param("now") Instant now);

    @Query(
            "SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP")
    long countActiveSessionsByUser(@Param("user") User user);

    @Query(
            "SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.ipAddress = :ipAddress AND rt.revoked = false AND rt.expiryDate > CURRENT_TIMESTAMP")
    List<RefreshToken> findActiveSessionsByUserAndIp(
            @Param("user") User user, @Param("ipAddress") String ipAddress);
}
