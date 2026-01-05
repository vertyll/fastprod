package com.vertyll.fastprod.modules.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.vertyll.fastprod.modules.auth.dto.*;
import com.vertyll.fastprod.modules.user.dto.ChangeEmailDto;
import com.vertyll.fastprod.modules.user.dto.ChangePasswordDto;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService extends BaseHttpService {

    private static final String AUTH_ENDPOINT = "/auth";

    public AuthService(
            @Value("${api.backend.url}") String backendUrl,
            ObjectMapper objectMapper,
            AuthTokenProvider authTokenProvider) {
        super(backendUrl, objectMapper, authTokenProvider);
    }

    public ApiResponse<Void> register(RegisterRequestDto registerRequest) throws Exception {
        return post(AUTH_ENDPOINT + "/register", registerRequest, Void.class);
    }

    public ApiResponse<AuthResponseDto> login(LoginRequestDto loginRequest) throws Exception {
        return post(AUTH_ENDPOINT + "/authenticate", loginRequest, AuthResponseDto.class);
    }

    public void verifyAccount(VerifyAccountRequestDto verifyAccountRequest) throws Exception {
        String endpoint = AUTH_ENDPOINT + "/verify?code=" + verifyAccountRequest.code();
        post(endpoint, null, Void.class);
    }

    public void resendVerificationCode(String email) throws Exception {
        ResendVerificationRequestDto request = new ResendVerificationRequestDto(email);
        post(AUTH_ENDPOINT + "/resend-verification-code", request, Void.class);
    }

    public ApiResponse<AuthResponseDto> refreshToken() throws Exception {
        return post(AUTH_ENDPOINT + "/refresh-token", null, AuthResponseDto.class);
    }

    public void logout() throws Exception {
        post(AUTH_ENDPOINT + "/logout", null, Void.class);
    }

    public void requestPasswordReset(String email) throws Exception {
        String endpoint = AUTH_ENDPOINT + "/reset-password-request?email=" + email;
        post(endpoint, null, Void.class);
    }

    public void resetPassword(String token, ResetPasswordRequestDto request) throws Exception {
        String endpoint = AUTH_ENDPOINT + "/reset-password?token=" + token;
        post(endpoint, request, Void.class);
    }

    public ApiResponse<Void> requestPasswordChange(ChangePasswordDto dto) throws Exception {
        return post(AUTH_ENDPOINT + "/change-password-request", dto, Void.class);
    }

    public ApiResponse<Void> verifyPasswordChange(String code) throws Exception {
        return post(AUTH_ENDPOINT + "/verify-password-change?code=" + code, null, Void.class);
    }

    public ApiResponse<Void> requestEmailChange(ChangeEmailDto dto) throws Exception {
        return post(AUTH_ENDPOINT + "/change-email-request", dto, Void.class);
    }

    public ApiResponse<AuthResponseDto> verifyEmailChange(String code) throws Exception {
        return post(
                AUTH_ENDPOINT + "/verify-email-change?code=" + code, null, AuthResponseDto.class);
    }
}
