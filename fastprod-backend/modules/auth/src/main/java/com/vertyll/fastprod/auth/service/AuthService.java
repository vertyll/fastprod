package com.vertyll.fastprod.auth.service;

import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.ChangeEmailRequestDto;
import com.vertyll.fastprod.auth.dto.ChangePasswordRequestDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.dto.ResetPasswordRequestDto;
import com.vertyll.fastprod.auth.dto.SessionResponseDto;

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
    void resendVerificationCode(String email) throws MessagingException;

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
