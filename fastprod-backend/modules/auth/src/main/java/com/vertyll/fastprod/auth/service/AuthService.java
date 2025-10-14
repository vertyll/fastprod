package com.vertyll.fastprod.auth.service;

import com.vertyll.fastprod.auth.dto.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuthService {
    @Transactional
    void register(RegisterRequestDto request) throws MessagingException;

    @Transactional
    AuthResponseDto authenticate(AuthRequestDto request, HttpServletRequest httpRequest, HttpServletResponse response);

    @Transactional
    AuthResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response);

    @Transactional
    void logout(HttpServletRequest request, HttpServletResponse response);

    @Transactional
    void logoutAllSessions(HttpServletRequest request, HttpServletResponse response);

    @Transactional(readOnly = true)
    List<SessionResponseDto> getUserActiveSessions(String email);

    @Transactional
    void verifyAccount(String code);

    @Transactional
    void requestEmailChange(ChangeEmailRequestDto request) throws MessagingException;

    @Transactional
    AuthResponseDto verifyEmailChange(String code, HttpServletRequest httpRequest, HttpServletResponse response);

    @Transactional
    void requestPasswordChange(ChangePasswordRequestDto request) throws MessagingException;

    @Transactional
    void verifyPasswordChange(String code);

    @Transactional
    void sendPasswordResetEmail(String email) throws MessagingException;

    @Transactional
    void resetPassword(String token, ResetPasswordRequestDto request);
}