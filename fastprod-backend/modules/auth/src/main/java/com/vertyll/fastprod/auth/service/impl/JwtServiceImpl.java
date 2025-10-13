package com.vertyll.fastprod.auth.service.impl;

import com.vertyll.fastprod.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.access-token.secret-key}")
    private String accessTokenSecretKey;

    @Value("${security.jwt.refresh-token.secret-key}")
    private String refreshTokenSecretKey;

    @Value("${security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${security.jwt.refresh-token.cookie-name}")
    private String refreshTokenCookieName;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpiration, ChronoUnit.MILLIS)))
                .signWith(getAccessTokenSigningKey())
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && isTokenUnexpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getRefreshTokenCookieName() {
        return refreshTokenCookieName;
    }

    @Override
    public long getRefreshTokenExpirationTime() {
        return refreshTokenExpiration;
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenExpiration, ChronoUnit.MILLIS)))
                .signWith(getRefreshTokenSigningKey())
                .compact();
    }

    @Override
    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsernameFromRefreshToken(token);
            return username.equals(userDetails.getUsername()) && isRefreshTokenUnexpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String extractUsernameFromRefreshToken(String token) {
        return extractClaimFromRefreshToken(token, Claims::getSubject);
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        try {
            return isRefreshTokenUnexpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenUnexpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private boolean isRefreshTokenUnexpired(String token) {
        return !extractExpirationFromRefreshToken(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Date extractExpirationFromRefreshToken(String token) {
        return extractClaimFromRefreshToken(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, getAccessTokenVerificationKey());
        return claimsResolver.apply(claims);
    }

    private <T> T extractClaimFromRefreshToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, getRefreshTokenVerificationKey());
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, SecretKey verificationKey) {
        return Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getAccessTokenSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessTokenSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getRefreshTokenSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshTokenSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getAccessTokenVerificationKey() {
        return getAccessTokenSigningKey();
    }

    private SecretKey getRefreshTokenVerificationKey() {
        return getRefreshTokenSigningKey();
    }
}
