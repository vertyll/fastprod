package com.vertyll.fastprod.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    String extractUsername(String token);

    String generateToken(UserDetails userDetails);

    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    String getRefreshTokenCookieName();

    long getRefreshTokenExpirationTime();

    String generateRefreshToken(UserDetails userDetails);

    boolean validateRefreshToken(String token, UserDetails userDetails);

    String extractUsernameFromRefreshToken(String token);

    boolean isRefreshTokenValid(String token);
}
