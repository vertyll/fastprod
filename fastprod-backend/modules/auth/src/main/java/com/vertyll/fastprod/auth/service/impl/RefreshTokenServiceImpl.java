package com.vertyll.fastprod.auth.service.impl;

import com.vertyll.fastprod.auth.dto.SessionInfoDto;
import com.vertyll.fastprod.auth.entity.RefreshToken;
import com.vertyll.fastprod.auth.repository.RefreshTokenRepository;
import com.vertyll.fastprod.auth.service.JwtService;
import com.vertyll.fastprod.auth.service.RefreshTokenService;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.sharedinfrastructure.util.HashUtil;
import com.vertyll.fastprod.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final String UNKNOWN = "unknown";
    private static final int MAX_IP_LENGTH = 45;
    private static final int MAX_USER_AGENT_LENGTH = 255;
    private static final String INVALID_REFRESH_TOKEN = "errors.auth.invalidRefreshToken";
    private static final String INVALID_REFRESH_TOKEN_SIGNATURE = "errors.auth.invalidRefreshToken";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String USER_AGENT = "User-Agent";
    private static final String REFRESH_TOKEN_NOT_FOUND = "errors.auth.invalidRefreshToken";

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final ObjectProvider<RefreshTokenService> selfProvider;

    @Override
    @Transactional
    public String createRefreshToken(User user, @Nullable String deviceInfo, HttpServletRequest request) {
        String tokenValue = jwtService.generateRefreshToken(user);

        String hashedToken = HashUtil.hashToken(tokenValue);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(hashedToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationTime()))
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

    @Override
    @Transactional
    public User validateRefreshToken(String token) {
        if (!jwtService.isRefreshTokenValid(token)) {
            log.error("Invalid or expired JWT refresh token");
            throw new ApiException(INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
        }

        String username = jwtService.extractUsernameFromRefreshToken(token);

        RefreshToken refreshToken = findTokenByValue(token, username);

        if (!jwtService.validateRefreshToken(token, refreshToken.getUser())) {
            log.error("Invalid JWT signature for refresh token, user: {}",
                    refreshToken.getUser().getEmail());
            throw new ApiException(INVALID_REFRESH_TOKEN_SIGNATURE, HttpStatus.UNAUTHORIZED);
        }

        refreshToken.markUsed(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getUser();
    }

    @Override
    @Transactional
    public String rotateRefreshToken(String oldToken, @Nullable String deviceInfo, HttpServletRequest request) {
        RefreshTokenService self = selfProvider.getObject();
        User user = self.validateRefreshToken(oldToken);

        self.revokeRefreshToken(oldToken);

        return self.createRefreshToken(user, deviceInfo, request);
    }

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
            refreshToken.revoke(Instant.now());
            refreshTokenRepository.save(refreshToken);

            log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
        } catch (ApiException e) {
            log.warn("Token not found for revocation: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevoked(user, false);
        tokens.forEach(token -> token.revoke(Instant.now()));
        refreshTokenRepository.saveAll(tokens);

        log.info("Revoked all refresh tokens for user: {} (count: {})",
                user.getEmail(), tokens.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(User user) {
        return refreshTokenRepository.findByUserAndRevoked(user, false)
                .stream()
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .toList();
    }

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

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }

    @SuppressFBWarnings(
            value = "SERVLET_HEADER",
            justification = "IP address is used only for audit logging, not security decisions. " +
                    "Value is sanitized before storage to prevent injection attacks.")
    private String extractIpAddress(HttpServletRequest request) {
        String ip;
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            ip = Iterables.get(Splitter.on(',').split(xForwardedFor), 0).trim();
        } else {
            String xRealIp = request.getHeader(X_REAL_IP);
            ip = (xRealIp != null && !xRealIp.isEmpty()) ? xRealIp : request.getRemoteAddr();
        }

        return sanitizeIpAddress(ip);
    }

    @SuppressFBWarnings(
            value = "SERVLET_HEADER_USER_AGENT",
            justification = "User-Agent is used only for audit logging and session display, " +
                    "not security decisions. Value is sanitized before storage.")
    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT);
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }

        return sanitizeUserAgent(userAgent);
    }

    private String sanitizeIpAddress(String ip) {
        if (ip.isEmpty()) {
            return UNKNOWN;
        }

        String sanitized = ip.replaceAll("\\p{Cntrl}", "_");

        return sanitized.substring(0, Math.min(sanitized.length(), MAX_IP_LENGTH));
    }

    private String sanitizeUserAgent(String userAgent) {
        if (userAgent.isEmpty()) {
            return UNKNOWN;
        }

        String sanitized = userAgent.replaceAll("\\p{Cntrl}", "_");

        return sanitized.substring(0, Math.min(sanitized.length(), MAX_USER_AGENT_LENGTH));
    }

    private RefreshToken findTokenByValue(String token, String username) {
        String hashedToken = HashUtil.hashToken(token);

        return refreshTokenRepository
                .findByUserEmailAndTokenAndRevoked(username, hashedToken, false)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in database for user: {}", username);
                    return new ApiException(REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED);
                });
    }
}
