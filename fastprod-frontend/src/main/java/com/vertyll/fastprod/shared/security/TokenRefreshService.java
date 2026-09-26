package com.vertyll.fastprod.shared.security;

import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.config.SecurityProperties;
import com.vertyll.fastprod.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenRefreshService {

    private final AuthService authService;
    private final SecurityService securityService;
    private final SecurityProperties securityProperties;

    private @Nullable Instant tokenExpirationTime;

    public void setTokenExpiration() {
        long expirationMs = securityProperties.accessToken().expiration();
        this.tokenExpirationTime = Instant.now().plusMillis(expirationMs);
        log.debug("Token expiration set to: {} ({}ms from now)", tokenExpirationTime, expirationMs);
    }

    public boolean shouldRefreshToken() {
        if (tokenExpirationTime == null) {
            return false;
        }

        long refreshBeforeMs = securityProperties.accessToken().refreshBeforeExpiry();
        Instant refreshThreshold = Instant.now().plusMillis(refreshBeforeMs);
        boolean shouldRefresh = refreshThreshold.isAfter(tokenExpirationTime);

        if (shouldRefresh) {
            log.debug("Token should be refreshed. Current time + {}ms is after expiration time", refreshBeforeMs);
        }

        return shouldRefresh;
    }

    public boolean refreshToken() {
        try {
            String cookieName = securityProperties.getRefreshTokenCookieName();
            log.debug("Attempting to refresh token using cookie: {}", cookieName);
            AuthResponseDto response = authService.refreshToken();

            if (response != null) {
                securityService.login(response);
                setTokenExpiration();
                log.info("Token refreshed successfully");
                return true;
            }

            log.warn("Token refresh returned null data");
            return false;

        } catch (ApiException e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            return false;
        }
    }

    public boolean ensureValidToken() {
        return securityService.isAuthenticated() && (!shouldRefreshToken() || refreshToken());
    }

    public void clearTokenExpiration() {
        this.tokenExpirationTime = null;
    }
}
