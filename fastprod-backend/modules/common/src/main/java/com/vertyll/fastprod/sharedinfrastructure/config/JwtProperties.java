package com.vertyll.fastprod.sharedinfrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "security.jwt")
@Validated
public record JwtProperties(AccessToken accessToken, RefreshToken refreshToken) {
    public record AccessToken(String secretKey, long expiration) {}

    public record RefreshToken(String secretKey, long expiration, String cookieName) {}
}
