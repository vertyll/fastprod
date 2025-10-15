package com.vertyll.fastprod.auth.service.impl;

import com.vertyll.fastprod.auth.config.JwtProperties;
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

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

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
                .expiration(Date.from(now.plus(jwtProperties.accessToken().expiration(), ChronoUnit.MILLIS)))
                .signWith(getAccessTokenSigningKey(), Jwts.SIG.HS256)
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
        return jwtProperties.refreshToken().cookieName();
    }

    @Override
    public long getRefreshTokenExpirationTime() {
        return jwtProperties.refreshToken().expiration();
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
                .expiration(Date.from(now.plus(jwtProperties.refreshToken().expiration(), ChronoUnit.MILLIS)))
                .signWith(getRefreshTokenSigningKey(), Jwts.SIG.HS256)
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
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.accessToken().secretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getRefreshTokenSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.refreshToken().secretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getAccessTokenVerificationKey() {
        return getAccessTokenSigningKey();
    }

    private SecretKey getRefreshTokenVerificationKey() {
        return getRefreshTokenSigningKey();
    }
}
