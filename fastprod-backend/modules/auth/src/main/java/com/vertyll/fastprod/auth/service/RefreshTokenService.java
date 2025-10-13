package com.vertyll.fastprod.auth.service;

import com.vertyll.fastprod.auth.dto.SessionInfoDto;
import com.vertyll.fastprod.auth.entity.RefreshToken;
import com.vertyll.fastprod.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface RefreshTokenService {
    @Transactional
    String createRefreshToken(User user, String deviceInfo, HttpServletRequest request);

    @Transactional()
    User validateRefreshToken(String token);

    @Transactional()
    String rotateRefreshToken(String oldToken, String deviceInfo, HttpServletRequest request);

    @Transactional()
    void revokeRefreshToken(String token);

    @Transactional()
    void revokeAllUserTokens(User user);

    @Transactional()
    List<RefreshToken> getUserActiveSessions(User user);

    @Transactional()
    List<SessionInfoDto> getUserSessionDetails(User user);

    @Transactional()
    void cleanupExpiredTokens();
}