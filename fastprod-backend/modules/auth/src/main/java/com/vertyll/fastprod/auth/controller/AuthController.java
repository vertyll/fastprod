package com.vertyll.fastprod.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.vertyll.fastprod.auth.dto.*;
import com.vertyll.fastprod.auth.service.AuthService;
import com.vertyll.fastprod.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "Auth management APIs")
class AuthController {

    private static final String IS_AUTHENTICATED = "isAuthenticated()";

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody @Valid RegisterRequestDto request) throws MessagingException {
        authService.register(request);
        return ApiResponse.buildResponse(null, "User registered successfully", HttpStatus.OK);
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> authenticate(
            @RequestBody @Valid AuthRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        AuthResponseDto authResponse = authService.authenticate(request, httpRequest, response);
        return ApiResponse.buildResponse(authResponse, "Authentication successful", HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token cookie")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
            HttpServletRequest request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.refreshToken(request, response);
        return ApiResponse.buildResponse(
                authResponse, "Token refreshed successfully", HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout from current session")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ApiResponse.buildResponse(null, "Logged out successfully", HttpStatus.OK);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all sessions")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            HttpServletRequest request, HttpServletResponse response) {
        authService.logoutAllSessions(request, response);
        return ApiResponse.buildResponse(
                null, "Logged out from all sessions successfully", HttpStatus.OK);
    }

    @GetMapping("/sessions")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Get all active sessions for the current user")
    public ResponseEntity<ApiResponse<List<SessionResponseDto>>> getSessions(
            @AuthenticationPrincipal String email) {
        List<SessionResponseDto> sessions = authService.getUserActiveSessions(email);
        return ApiResponse.buildResponse(
                sessions, "Active sessions retrieved successfully", HttpStatus.OK);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify user account with code")
    public ResponseEntity<ApiResponse<Void>> verifyAccount(@RequestParam String code) {
        authService.verifyAccount(code);
        return ApiResponse.buildResponse(null, "Account verified successfully", HttpStatus.OK);
    }

    @PostMapping("/resend-verification-code")
    @Operation(summary = "Resend verification code to user email")
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(
            @RequestBody @Valid ResendVerificationRequestDto request) throws MessagingException {
        authService.resendVerificationCode(request.email());
        return ApiResponse.buildResponse(
                null, "Verification code sent successfully", HttpStatus.OK);
    }

    @PostMapping("/change-email-request")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Request email change, sends verification to new email")
    public ResponseEntity<ApiResponse<Void>> requestEmailChange(
            @RequestBody @Valid ChangeEmailRequestDto request) throws MessagingException {
        authService.requestEmailChange(request);
        return ApiResponse.buildResponse(
                null, "Email change verification sent to new email", HttpStatus.OK);
    }

    @PostMapping("/verify-email-change")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Verify email change with code")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyEmailChange(
            @RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.verifyEmailChange(code, request, response);
        return ApiResponse.buildResponse(authResponse, "Email changed successfully", HttpStatus.OK);
    }

    @PostMapping("/change-password-request")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Request password change, sends verification email")
    public ResponseEntity<ApiResponse<Void>> requestPasswordChange(
            @RequestBody @Valid ChangePasswordRequestDto request) throws MessagingException {
        authService.requestPasswordChange(request);
        return ApiResponse.buildResponse(
                null, "Password change verification sent to email", HttpStatus.OK);
    }

    @PostMapping("/verify-password-change")
    @PreAuthorize(IS_AUTHENTICATED)
    @Operation(summary = "Verify password change with code")
    public ResponseEntity<ApiResponse<Void>> verifyPasswordChange(@RequestParam String code) {
        authService.verifyPasswordChange(code);
        return ApiResponse.buildResponse(null, "Password changed successfully", HttpStatus.OK);
    }

    @PostMapping("/reset-password-request")
    @Operation(summary = "Request password reset for a forgotten password")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @RequestParam @Email(message = "Email should be valid") String email)
            throws MessagingException {
        authService.sendPasswordResetEmail(email);
        return ApiResponse.buildResponse(
                null, "Password reset instructions sent to email", HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token, @RequestBody @Valid ResetPasswordRequestDto request) {
        authService.resetPassword(token, request);
        return ApiResponse.buildResponse(null, "Password reset successfully", HttpStatus.OK);
    }
}
