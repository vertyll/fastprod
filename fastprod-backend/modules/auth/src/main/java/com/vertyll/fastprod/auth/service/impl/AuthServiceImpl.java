package com.vertyll.fastprod.auth.service.impl;

import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.ChangeEmailRequestDto;
import com.vertyll.fastprod.auth.dto.ChangePasswordRequestDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.dto.ResetPasswordRequestDto;
import com.vertyll.fastprod.auth.dto.SessionResponseDto;
import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.auth.mapper.AuthMapper;
import com.vertyll.fastprod.auth.service.AuthService;
import com.vertyll.fastprod.auth.service.JwtService;
import com.vertyll.fastprod.auth.service.RefreshTokenService;
import com.vertyll.fastprod.auth.service.VerificationTokenService;
import com.vertyll.fastprod.sharedinfrastructure.config.CookieProperties;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.email.service.EmailService;
import com.vertyll.fastprod.email.enums.EmailTemplateName;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.service.UserService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
class AuthServiceImpl implements AuthService {

    private static final String USER_NOT_FOUND_MESSAGE = "errors.user.notFound";
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final String EMAIL_ALREADY_REGISTERED = "errors.user.emailTaken";
    private static final String ACCOUNT_ACTIVATION = "Account activation";
    private static final String ACCOUNT_NOT_VERIFIED = "errors.auth.accountNotVerified";
    private static final String REFRESH_TOKEN_NOT_FOUND = "errors.auth.invalidRefreshToken";
    private static final String ACCOUNT_ALREADY_VERIFIED = "errors.auth.accountAlreadyVerified";
    private static final String USER_NOT_AUTHENTICATED = "errors.auth.authenticationRequired";
    private static final String INVALID_CURRENT_PASSWORD = "errors.auth.invalidCurrentPassword";
    private static final String EMAIL_ALREADY_IN_USE = "errors.user.emailTaken";
    private static final String EMAIL_CHANGE_VERIFICATION = "Email Change Verification";
    private static final String NEW_EMAIL_NOT_FOUND = "errors.verification.missingNewEmail";
    private static final String PASSWORD_CHANGE_VERIFICATION = "Password Change Verification";
    private static final String NEW_PASSWORD_NOT_FOUND = "errors.verification.missingNewPassword";
    private static final String PASSWORD_RESET = "Password Reset";
    private static final String SET_COOKIE = "Set-Cookie";

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final CookieProperties cookieProperties;
    private final AuthMapper authMapper;

    @Override
    @Transactional(rollbackFor = MessagingException.class)
    public void register(RegisterRequestDto request) throws MessagingException {
        if (userService.existsByEmail(request.email())) {
            throw new ApiException(EMAIL_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST);
        }

        User user = authMapper.toUserEntity(request);
               
        user.changePassword(passwordEncoder.encode(request.password()));
        user.assignRoles(Set.of(roleService.getOrCreateDefaultRole(RoleType.USER)));

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
                ACCOUNT_ACTIVATION
        );
    }

    @Override
    @Transactional
    public AuthResponseDto authenticate(
            AuthRequestDto request,
            HttpServletRequest httpRequest,
            @Nullable HttpServletResponse response
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userService.findByEmailWithRoles(request.email())
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!user.isVerified()) {
            throw new ApiException(ACCOUNT_NOT_VERIFIED, HttpStatus.FORBIDDEN);
        }

        Map<String, Object> claims = createClaimsWithRoles(user);
        String jwtToken = jwtService.generateToken(claims, user);

        if (response != null) {
            String refreshToken = refreshTokenService.createRefreshToken(user, request.deviceInfo(), httpRequest);
            addRefreshTokenCookie(response, refreshToken);
        }

        return authMapper.toAuthResponseDto(jwtToken, BEARER_TOKEN_TYPE);
    }

    @Override
    @Transactional
    public AuthResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request)
                .orElseThrow(() -> new ApiException(REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        User user = refreshTokenService.validateRefreshToken(refreshToken);

        Map<String, Object> claims = createClaimsWithRoles(user);
        String accessToken = jwtService.generateToken(claims, user);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, null, request);

        addRefreshTokenCookie(response, newRefreshToken);

        return authMapper.toAuthResponseDto(accessToken, BEARER_TOKEN_TYPE);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        extractRefreshTokenFromCookies(request).ifPresent(refreshTokenService::revokeRefreshToken);

        deleteRefreshTokenCookie(response);
    }

    @Override
    @Transactional
    public void logoutAllSessions(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request)
                .orElseThrow(() -> new ApiException(REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        User user = refreshTokenService.validateRefreshToken(refreshToken);
        refreshTokenService.revokeAllUserTokens(user);
        deleteRefreshTokenCookie(response);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponseDto> getUserActiveSessions(String email) {
        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        return refreshTokenService.getUserSessionDetails(user)
                .stream()
                .map(session -> authMapper.toSessionResponseDto(session, false))
                .toList();
    }

    @Override
    @Transactional
    public void verifyAccount(String code) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                code,
                VerificationTokenType.ACCOUNT_ACTIVATION
        );

        User user = verificationToken.getUser();

        if (user.isVerified()) {
            throw new ApiException(ACCOUNT_ALREADY_VERIFIED, HttpStatus.BAD_REQUEST);
        }

        user.markVerified();
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);
    }

    @Override
    @Transactional(rollbackFor = MessagingException.class)
    public void resendVerificationCode(String email) throws MessagingException {
        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (user.isVerified()) {
            throw new ApiException(ACCOUNT_ALREADY_VERIFIED, HttpStatus.BAD_REQUEST);
        }

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
                ACCOUNT_ACTIVATION
        );
    }

    @Override
    @Transactional(rollbackFor = MessagingException.class)
    public void requestEmailChange(ChangeEmailRequestDto request) throws MessagingException {
        String currentEmail = getCurrentUserEmail();

        User user = userService.findByEmailWithRoles(currentEmail)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException(INVALID_CURRENT_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        if (userService.existsByEmail(request.newEmail())) {
            throw new ApiException(EMAIL_ALREADY_IN_USE, HttpStatus.BAD_REQUEST);
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
                EMAIL_CHANGE_VERIFICATION
        );
    }

    @Override
    @Transactional
    public AuthResponseDto verifyEmailChange(String code, HttpServletRequest httpRequest, HttpServletResponse response) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                code,
                VerificationTokenType.EMAIL_CHANGE
        );

        String newEmail = verificationToken.getAdditionalData();
        if (newEmail == null) {
            throw new ApiException(NEW_EMAIL_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        user.changeEmail(newEmail);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);

        Map<String, Object> claims = createClaimsWithRoles(user);
        String jwtToken = jwtService.generateToken(claims, user);
        String refreshToken = refreshTokenService.createRefreshToken(user, null, httpRequest);
        addRefreshTokenCookie(response, refreshToken);

        return authMapper.toAuthResponseDto(jwtToken, BEARER_TOKEN_TYPE);
    }

    @Override
    @Transactional(rollbackFor = MessagingException.class)
    public void requestPasswordChange(ChangePasswordRequestDto request) throws MessagingException {
        String email = getCurrentUserEmail();

        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException(INVALID_CURRENT_PASSWORD, HttpStatus.BAD_REQUEST);
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
                PASSWORD_CHANGE_VERIFICATION
        );
    }

    @Override
    @Transactional
    public void verifyPasswordChange(String code) {
        VerificationToken verificationToken = verificationTokenService.getValidToken(
                code,
                VerificationTokenType.PASSWORD_CHANGE
        );

        String newPasswordHash = verificationToken.getAdditionalData();
        if (newPasswordHash == null) {
            throw new ApiException(NEW_PASSWORD_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        user.changePassword(newPasswordHash);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);
    }

    @Override
    @Transactional(rollbackFor = MessagingException.class)
    public void sendPasswordResetEmail(String email) throws MessagingException {
        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

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
                PASSWORD_RESET
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

        user.changePassword(newPasswordHash);
        userService.saveUser(user);

        verificationTokenService.markTokenAsUsed(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtService.getRefreshTokenCookieName(), token)
                .httpOnly(cookieProperties.httpOnly())
                .secure(cookieProperties.secure())
                .path(cookieProperties.path())
                .maxAge(jwtService.getRefreshTokenExpirationTime() / 1000)
                .sameSite(cookieProperties.sameSite())
                .build();

        response.addHeader(SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtService.getRefreshTokenCookieName(), "")
                .httpOnly(cookieProperties.httpOnly())
                .secure(cookieProperties.secure())
                .path(cookieProperties.path())
                .maxAge(0)
                .sameSite(cookieProperties.sameSite())
                .build();

        response.addHeader(SET_COOKIE, cookie.toString());
    }

    @SuppressFBWarnings(
        value = "COOKIE_USAGE",
        justification = "Refresh token is stored in Secure, HttpOnly cookie and used only server-side"
    )
    private Optional<String> extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        String cookieName = jwtService.getRefreshTokenCookieName();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private String getCurrentUserEmail() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new ApiException(USER_NOT_AUTHENTICATED, HttpStatus.UNAUTHORIZED));
    }

    private Map<String, Object> createClaimsWithRoles(User user) {
        Map<String, Object> claims = new ConcurrentHashMap<>();
        List<RoleType> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        claims.put("roles", roles);
        return claims;
    }
}
