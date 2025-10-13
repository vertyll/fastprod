package com.vertyll.fastprod.auth;

import com.vertyll.fastprod.auth.dto.SessionInfoDto;
import com.vertyll.fastprod.auth.entity.RefreshToken;
import com.vertyll.fastprod.auth.repository.RefreshTokenRepository;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Creates a new refresh token for the given user using JWT.
     */
    @Override
    @Transactional
    public String createRefreshToken(User user, String deviceInfo, HttpServletRequest request) {
        String tokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationTime()))
                .isRevoked(false)
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
     */
    @Override
    @Transactional
    public User validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Refresh token not found", HttpStatus.UNAUTHORIZED));

        if (refreshToken.isRevoked()) {
            log.warn("Attempted to use revoked refresh token for user: {}",
                    refreshToken.getUser().getEmail());
            throw new ApiException("Refresh token revoked", HttpStatus.UNAUTHORIZED);
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Attempted to use expired refresh token for user: {}",
                    refreshToken.getUser().getEmail());
            throw new ApiException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

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
        User user = validateRefreshToken(oldToken);

        // Revoke the old token
        revokeRefreshToken(oldToken);

        // Create new refresh token
        return createRefreshToken(user, deviceInfo, request);
    }

    /**
     * Revokes a specific refresh token.
     */
    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshToken.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(refreshToken);

                    log.info("Revoked refresh token for user: {}",
                            refreshToken.getUser().getEmail());
                });
    }

    /**
     * Revokes all refresh tokens for a user.
     */
    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndIsRevoked(user, false);
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
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(User user) {
        return refreshTokenRepository.findByUserAndIsRevoked(user, false)
                .stream()
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .filter(token -> jwtService.isRefreshTokenValid(token.getToken()))
                .collect(Collectors.toList());
    }

    /**
     * Gets session details including security information.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SessionInfoDto> getUserSessionDetails(User user) {
        return getUserActiveSessions(user).stream()
                .map(token -> SessionInfoDto.builder()
                        .id(token.getId())
                        .deviceInfo(token.getDeviceInfo())
                        .ipAddress(token.getIpAddress())
                        .userAgent(token.getUserAgent())
                        .createdAt(token.getCreatedAt())
                        .lastUsedAt(token.getLastUsedAt())
                        .expiresAt(token.getExpiryDate())
                        .build())
                .collect(Collectors.toList());
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

    /**
     * Extracts the client IP address from the request.
     * Handles proxy headers like X-Forwarded-For.
     */
    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Extracts the User-Agent header from the request.
     */
    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }
}
