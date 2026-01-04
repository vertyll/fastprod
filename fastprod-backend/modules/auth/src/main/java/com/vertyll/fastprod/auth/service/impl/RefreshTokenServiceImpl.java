package com.vertyll.fastprod.auth.service.impl;

import com.vertyll.fastprod.auth.dto.SessionInfoDto;
import com.vertyll.fastprod.auth.entity.RefreshToken;
import com.vertyll.fastprod.auth.repository.RefreshTokenRepository;
import com.vertyll.fastprod.auth.service.JwtService;
import com.vertyll.fastprod.auth.service.RefreshTokenService;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.common.util.HashUtil;
import com.vertyll.fastprod.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final String UNKNOWN = "unknown";

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final ObjectProvider<RefreshTokenService> selfProvider;

    @Override
    @Transactional
    public String createRefreshToken(User user, String deviceInfo, HttpServletRequest request) {
        String tokenValue = jwtService.generateRefreshToken(user);

        String hashedToken = HashUtil.hashToken(tokenValue);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(hashedToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationTime()))
                .revoked(false)
                .deviceInfo(deviceInfo)
                .ipAddress(extractIpAddress(request))
                .userAgent(extractUserAgent(request))
                .lastUsedAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("Created refresh token for user: {} from IP: {}",
                user.getEmail(), refreshToken.getIpAddress());

        return tokenValue;
    }

    /**
     * Validates a refresh token and returns the associated user if valid.
     * Compares the provided token with hashed tokens in database.
     */
    @Override
    @Transactional
    public User validateRefreshToken(String token) {
        // Validate JWT signature and expiration
        if (!jwtService.isRefreshTokenValid(token)) {
            log.error("Invalid or expired JWT refresh token");
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        // Extract username from token
        String username = jwtService.extractUsernameFromRefreshToken(token);

        // Find the matching token in database
        RefreshToken refreshToken = findTokenByValue(token, username);

        // Validate JWT signature using refresh token secret key
        if (!jwtService.validateRefreshToken(token, refreshToken.getUser())) {
            log.error("Invalid JWT signature for refresh token, user: {}",
                    refreshToken.getUser().getEmail());
            throw new ApiException("Invalid refresh token signature", HttpStatus.UNAUTHORIZED);
        }

        // Update last used timestamp
        refreshToken.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getUser();
    }

    /**
     * Rotates a refresh token - revokes the old one and creates a new one.
     */
    @Override
    @Transactional
    public String rotateRefreshToken(String oldToken, String deviceInfo, HttpServletRequest request) {
        RefreshTokenService self = selfProvider.getObject();
        User user = self.validateRefreshToken(oldToken);

        // Revoke the old token
        self.revokeRefreshToken(oldToken);

        // Create new refresh token
        return self.createRefreshToken(user, deviceInfo, request);
    }

    /**
     * Revokes a specific refresh token.
     */
    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        if (!jwtService.isRefreshTokenValid(token)) {
            log.warn("Attempted to revoke invalid or expired JWT token");
            return;
        }

        String username = jwtService.extractUsernameFromRefreshToken(token);

        try {
            RefreshToken refreshToken = findTokenByValue(token, username);
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);

            log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
        } catch (ApiException e) {
            log.warn("Token not found for revocation: {}", e.getMessage());
        }
    }

    /**
     * Revokes all refresh tokens for a user.
     */
    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevoked(user, false);
        tokens.forEach(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
        });
        refreshTokenRepository.saveAll(tokens);

        log.info("Revoked all refresh tokens for user: {} (count: {})",
                user.getEmail(), tokens.size());
    }

    /**
     * Gets all active sessions for a user.
     * Note: We can't validate individual JWT signatures here because tokens are hashed.
     * We rely on database state (expiry, revoked) for session listing.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(User user) {
        return refreshTokenRepository.findByUserAndRevoked(user, false)
                .stream()
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .toList();
    }

    /**
     * Gets session details including security information.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SessionInfoDto> getUserSessionDetails(User user) {
        return selfProvider.getObject().getUserActiveSessions(user).stream()
                .map(token -> SessionInfoDto.builder()
                        .id(token.getId())
                        .deviceInfo(token.getDeviceInfo())
                        .ipAddress(token.getIpAddress())
                        .userAgent(token.getUserAgent())
                        .createdAt(token.getCreatedAt())
                        .lastUsedAt(token.getLastUsedAt())
                        .expiresAt(token.getExpiryDate())
                        .build())
                .toList();
    }

    /**
     * Scheduled task to delete expired tokens.
     */
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }

    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return Iterables.get(Splitter.on(',').split(xForwardedFor), 0).trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : UNKNOWN;
    }

    private RefreshToken findTokenByValue(String token, String username) {
        String hashedToken = HashUtil.hashToken(token);

        return refreshTokenRepository
                .findByUserEmailAndTokenAndRevoked(username, hashedToken, false)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in database for user: {}", username);
                    return new ApiException("Refresh token not found", HttpStatus.UNAUTHORIZED);
                });
    }
}
