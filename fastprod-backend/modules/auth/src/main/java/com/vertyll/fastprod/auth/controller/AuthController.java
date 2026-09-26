package com.vertyll.fastprod.auth.controller;

import java.util.List;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.vertyll.fastprod.auth.dto.*;
import com.vertyll.fastprod.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Auth management APIs")
public class AuthController {

    private static final String IS_AUTHENTICATED = "isAuthenticated()";

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDto request) throws MessagingException {
        authService.register(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<AuthResponseDto> authenticate(
        @RequestBody @Valid AuthRequestDto request,
        HttpServletRequest httpRequest,
        HttpServletResponse response
    ) {
        AuthResponseDto authResponse = authService.authenticate(request, httpRequest, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<AuthResponseDto> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.refreshToken(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current session")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all sessions")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request, HttpServletResponse response) {
        authService.logoutAllSessions(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Get all active sessions for the current user")
    public ResponseEntity<List<SessionResponseDto>> getSessions(@AuthenticationPrincipal String email) {
        List<SessionResponseDto> sessions = authService.getUserActiveSessions(email);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify user account with code")
    public ResponseEntity<Void> verifyAccount(@RequestParam String code) {
        authService.verifyAccount(code);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification-code")
    @Operation(summary = "Resend verification code to user email")
    public ResponseEntity<Void> resendVerificationCode(
        @RequestBody @Valid ResendVerificationRequestDto request
    ) throws MessagingException {
        authService.resendVerificationCode(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-email-request")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Request email change, sends verification to new email")
    public ResponseEntity<Void> requestEmailChange(
        @RequestBody @Valid ChangeEmailRequestDto request
    ) throws MessagingException {
        authService.requestEmailChange(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email-change")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Verify email change with code")
    public ResponseEntity<AuthResponseDto> verifyEmailChange(
        @RequestParam String code,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        AuthResponseDto authResponse = authService.verifyEmailChange(code, request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/change-password-request")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Request password change, sends verification email")
    public ResponseEntity<Void> requestPasswordChange(
        @RequestBody @Valid ChangePasswordRequestDto request
    ) throws MessagingException {
        authService.requestPasswordChange(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-password-change")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Verify password change with code")
    public ResponseEntity<Void> verifyPasswordChange(@RequestParam String code) {
        authService.verifyPasswordChange(code);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password-request")
    @Operation(summary = "Request password reset for a forgotten password")
    public ResponseEntity<Void> requestPasswordReset(
        @RequestParam @Email(message = "Email should be valid") String email
    ) throws MessagingException {
        authService.sendPasswordResetEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using reset token")
    public ResponseEntity<Void> resetPassword(
        @RequestParam String token,
        @RequestBody @Valid ResetPasswordRequestDto request
    ) {
        authService.resetPassword(token, request);
        return ResponseEntity.noContent().build();
    }
}
