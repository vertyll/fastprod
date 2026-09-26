package com.vertyll.fastprod.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security.jwt")
public record SecurityProperties(@DefaultValue AccessToken accessToken, @DefaultValue RefreshToken refreshToken) {

    public record AccessToken(
        @DefaultValue("900000") long expiration,
        @DefaultValue("120000") long refreshBeforeExpiry
    ) {
    }

    public record RefreshToken(@DefaultValue("refresh_token") String cookieName) {
    }

    public String getRefreshTokenCookieName() {
        return refreshToken.cookieName();
    }
}
