package com.vertyll.fastprod.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class SecurityProperties {

    private final AccessToken accessToken = new AccessToken();
    private final RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class AccessToken {
        private long expiration = 900000L;
        private long refreshBeforeExpiry = 120000L;
    }

    @Data
    public static class RefreshToken {
        private String cookieName = "refresh_token";
    }

    public String getRefreshTokenCookieName() {
        return refreshToken.getCookieName();
    }
}
