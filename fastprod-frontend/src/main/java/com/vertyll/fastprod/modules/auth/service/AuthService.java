package com.vertyll.fastprod.modules.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.modules.auth.dto.*;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.service.BaseHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService extends BaseHttpService {

    private static final String AUTH_ENDPOINT = "/auth";

    public AuthService(@Value("${api.backend.url}") String backendUrl, ObjectMapper objectMapper) {
        super(backendUrl, objectMapper);
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

    public ApiResponse<Void> logout() throws Exception {
        return post(AUTH_ENDPOINT + "/logout", null, Void.class);
    }
}
