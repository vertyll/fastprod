package com.vertyll.fastprod.shared.security;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.config.SecurityProperties;
import com.vertyll.fastprod.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenRefreshService {

    private final AuthService authService;
    private final SecurityService securityService;
    private final SecurityProperties securityProperties;

    private Instant tokenExpirationTime;

    public void setTokenExpiration() {
        long expirationMs = securityProperties.getAccessToken().getExpiration();
        this.tokenExpirationTime = Instant.now().plusMillis(expirationMs);
        log.debug("Token expiration set to: {} ({}ms from now)", tokenExpirationTime, expirationMs);
    }

    public boolean shouldRefreshToken() {
        if (tokenExpirationTime == null) {
            return false;
        }

        long refreshBeforeMs = securityProperties.getAccessToken().getRefreshBeforeExpiry();
        Instant refreshThreshold = Instant.now().plusMillis(refreshBeforeMs);
        boolean shouldRefresh = refreshThreshold.isAfter(tokenExpirationTime);

        if (shouldRefresh) {
            log.debug(
                    "Token should be refreshed. Current time + {}ms is after expiration time",
                    refreshBeforeMs);
        }

        return shouldRefresh;
    }

    public boolean refreshToken() {
        try {
            String cookieName = securityProperties.getRefreshTokenCookieName();
            log.debug("Attempting to refresh token using cookie: {}", cookieName);
            ApiResponse<AuthResponseDto> response = authService.refreshToken();

            if (response.data() != null) {
                securityService.login(response.data());
                setTokenExpiration();
                log.info("Token refreshed successfully");
                return true;
            }

            log.warn("Token refresh returned null data");
            return false;

        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            return false;
        }
    }

    public boolean ensureValidToken() {
        if (!securityService.isAuthenticated()) {
            return false;
        }

        if (shouldRefreshToken()) {
            return refreshToken();
        }

        return true;
    }

    public void clearTokenExpiration() {
        this.tokenExpirationTime = null;
    }
}
