package com.vertyll.fastprod.auth;

import com.vertyll.fastprod.auth.dto.*;
import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.email.EmailService;
import com.vertyll.fastprod.email.enums.EmailTemplateName;
import com.vertyll.fastprod.role.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    @Transactional
    public void register(RegisterRequestDto request) throws MessagingException {
        if (userService.existsByEmail(request.email())) {
            throw new ApiException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(roleService.getOrCreateDefaultRole("USER")))
                .isVerified(false)
                .build();

        userService.saveUser(user);

        String verificationCode = verificationTokenService.createVerificationToken(
                user,
                VerificationTokenType.ACCOUNT_ACTIVATION,
                null
        );

        emailService.sendEmail(
                user.getEmail(),
                user.getFirstName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                verificationCode,
                "Account activation"
        );
    }

    @Override
    @Transactional
    public AuthResponseDto authenticate(AuthRequestDto request, HttpServletRequest httpRequest, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userService.findByEmailWithRoles(request.email())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!user.isVerified()) {
            throw new ApiException("Account not verified", HttpStatus.FORBIDDEN);
        }

        String jwtToken = jwtService.generateToken(user);

        if (response != null) {
            String refreshToken = refreshTokenService.createRefreshToken(user, request.deviceInfo(), httpRequest);
            addRefreshTokenCookie(response, refreshToken);
        }

        return AuthResponseDto.mapToDto(jwtToken, "Bearer");
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            throw new ApiException("Refresh token not found", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshTokenService.validateRefreshToken(refreshToken);
        String accessToken = jwtService.generateToken(user);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, null, request);

        addRefreshTokenCookie(response, newRefreshToken);

        return AuthResponseDto.mapToDto(accessToken, "Bearer");
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }

        deleteRefreshTokenCookie(response);
    }

    @Override
    @Transactional
    public void logoutAllSessions(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            User user = refreshTokenService.validateRefreshToken(refreshToken);
            refreshTokenService.revokeAllUserTokens(user);
            deleteRefreshTokenCookie(response);
        } else {
            throw new ApiException("Refresh token not found", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserActiveSessions(String email) {
        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        return refreshTokenService.getUserSessionDetails(user)
                .stream()
                .map(session -> {
                    Map<String, Object> sessionMap = new HashMap<>();
                    sessionMap.put("id", session.id());
                    sessionMap.put("deviceInfo", session.deviceInfo() != null ? session.deviceInfo() : "Unknown device");
                    sessionMap.put("ipAddress", session.ipAddress());
                    sessionMap.put("userAgent", session.userAgent());
                    sessionMap.put("createdAt", session.createdAt());
                    sessionMap.put("lastUsedAt", session.lastUsedAt());
                    sessionMap.put("expiresAt", session.expiresAt());
                    return sessionMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void verifyAccount(String code) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                code,
                VerificationTokenType.ACCOUNT_ACTIVATION
        );

        User user = verificationToken.getUser();
        user.setVerified(true);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);
    }

    @Override
    @Transactional
    public void requestEmailChange(ChangeEmailRequestDto request) throws MessagingException {
        Authentication authentication = getCurrentAuthentication();
        String currentEmail = authentication.getName();

        User user = userService.findByEmailWithRoles(currentEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException("Invalid current password", HttpStatus.BAD_REQUEST);
        }

        if (userService.existsByEmail(request.newEmail())) {
            throw new ApiException("Email already in use", HttpStatus.BAD_REQUEST);
        }

        String verificationCode = verificationTokenService.createVerificationToken(
                user,
                VerificationTokenType.EMAIL_CHANGE,
                request.newEmail()
        );

        emailService.sendEmail(
                request.newEmail(),
                user.getFirstName(),
                EmailTemplateName.CHANGE_EMAIL,
                verificationCode,
                "Email Change Verification"
        );
    }

    @Override
    @Transactional
    public AuthResponseDto verifyEmailChange(String code, HttpServletRequest httpRequest, HttpServletResponse response) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                code,
                VerificationTokenType.EMAIL_CHANGE
        );

        User user = verificationToken.getUser();
        String newEmail = verificationToken.getAdditionalData();

        if (newEmail == null) {
            throw new ApiException("New email not found", HttpStatus.BAD_REQUEST);
        }

        user.setEmail(newEmail);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user, null, httpRequest);
        addRefreshTokenCookie(response, refreshToken);

        return AuthResponseDto.mapToDto(jwtToken, "Bearer");
    }

    @Override
    @Transactional
    public void requestPasswordChange(ChangePasswordRequestDto request) throws MessagingException {
        Authentication authentication = getCurrentAuthentication();
        String email = authentication.getName();

        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException("Invalid current password", HttpStatus.BAD_REQUEST);
        }

        String encodedNewPassword = passwordEncoder.encode(request.newPassword());

        String verificationCode = verificationTokenService.createVerificationToken(
                user,
                VerificationTokenType.PASSWORD_CHANGE,
                encodedNewPassword
        );

        emailService.sendEmail(
                user.getEmail(),
                user.getFirstName(),
                EmailTemplateName.CHANGE_PASSWORD,
                verificationCode,
                "Password Change Verification"
        );
    }

    @Override
    @Transactional
    public void verifyPasswordChange(String code) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                code,
                VerificationTokenType.PASSWORD_CHANGE
        );

        User user = verificationToken.getUser();
        String newPasswordHash = verificationToken.getAdditionalData();

        if (newPasswordHash == null) {
            throw new ApiException("New password not found", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(newPasswordHash);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) throws MessagingException {
        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        String verificationCode = verificationTokenService.createVerificationToken(
                user,
                VerificationTokenType.PASSWORD_RESET,
                null
        );

        emailService.sendEmail(
                email,
                user.getFirstName(),
                EmailTemplateName.RESET_PASSWORD,
                verificationCode,
                "Password Reset"
        );
    }

    @Override
    @Transactional
    public void resetPassword(String token, ResetPasswordRequestDto request) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                token,
                VerificationTokenType.PASSWORD_RESET
        );

        User user = verificationToken.getUser();
        String newPasswordHash = passwordEncoder.encode(request.newPassword());

        user.setPassword(newPasswordHash);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(jwtService.getRefreshTokenCookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtService.getRefreshTokenExpirationTime() / 1000));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtService.getRefreshTokenCookieName(), "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(jwtService.getRefreshTokenCookieName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
