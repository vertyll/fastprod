package com.vertyll.fastprod.modules.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.dto.LoginRequestDto;
import com.vertyll.fastprod.modules.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.modules.auth.dto.ResendVerificationRequestDto;
import com.vertyll.fastprod.modules.auth.dto.ResetPasswordRequestDto;
import com.vertyll.fastprod.modules.auth.dto.VerifyAccountRequestDto;
import com.vertyll.fastprod.modules.user.dto.ChangeEmailDto;
import com.vertyll.fastprod.modules.user.dto.ChangePasswordDto;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public class AuthService extends BaseHttpService {

    private static final String AUTH_ENDPOINT = "/auth";

    public AuthService(
        @Value("${api.backend.url}") String backendUrl,
        ObjectMapper objectMapper,
        AuthTokenProvider authTokenProvider
    ) {
        super(backendUrl, objectMapper, authTokenProvider);
    }

    public void register(RegisterRequestDto registerRequest) {
        post(AUTH_ENDPOINT + "/register", registerRequest, Void.class);
    }

    public AuthResponseDto login(LoginRequestDto loginRequest) {
        return requireNonNull(post(AUTH_ENDPOINT + "/authenticate", loginRequest, AuthResponseDto.class));
    }

    public void verifyAccount(VerifyAccountRequestDto verifyAccountRequest) {
        String endpoint = AUTH_ENDPOINT + "/verify?code=" + encode(verifyAccountRequest.code());
        post(endpoint, Void.class);
    }

    public void resendVerificationCode(String email) {
        ResendVerificationRequestDto request = new ResendVerificationRequestDto(email);
        post(AUTH_ENDPOINT + "/resend-verification-code", request, Void.class);
    }

    public AuthResponseDto refreshToken() {
        return requireNonNull(post(AUTH_ENDPOINT + "/refresh-token", AuthResponseDto.class));
    }

    public void logout() {
        post(AUTH_ENDPOINT + "/logout", Void.class);
    }

    public void requestPasswordReset(String email) {
        String endpoint = AUTH_ENDPOINT + "/reset-password-request?email=" + encode(email);
        post(endpoint, Void.class);
    }

    public void resetPassword(String token, ResetPasswordRequestDto request) {
        String endpoint = AUTH_ENDPOINT + "/reset-password?token=" + encode(token);
        post(endpoint, request, Void.class);
    }

    public void requestPasswordChange(ChangePasswordDto dto) {
        post(AUTH_ENDPOINT + "/change-password-request", dto, Void.class);
    }

    public void verifyPasswordChange(String code) {
        post(AUTH_ENDPOINT + "/verify-password-change?code=" + encode(code), Void.class);
    }

    public void requestEmailChange(ChangeEmailDto dto) {
        post(AUTH_ENDPOINT + "/change-email-request", dto, Void.class);
    }

    public AuthResponseDto verifyEmailChange(String code) {
        return requireNonNull(post(AUTH_ENDPOINT + "/verify-email-change?code=" + encode(code), AuthResponseDto.class));
    }
}
