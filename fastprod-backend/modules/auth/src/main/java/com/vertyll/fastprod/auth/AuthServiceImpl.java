package com.vertyll.fastprod.auth;

import com.vertyll.fastprod.auth.dto.*;
import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.auth.repository.VerificationTokenRepository;
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

import java.time.LocalDateTime;
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
    private final VerificationTokenRepository tokenRepository;
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

        User user = User
                .builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(roleService.getOrCreateDefaultRole("USER")))
                .enabled(false)
                .build();

        userService.saveUser(user);

        String verificationCode = generateVerificationCode();
        createVerificationToken(user, verificationCode, VerificationTokenType.ACCOUNT_ACTIVATION, null);
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

        if (!user.isEnabled()) {
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
        VerificationToken verificationToken = getVerificationTokenByCode(code);

        if (verificationToken.isUsed()) {
            throw new ApiException("Verification code already used", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Verification code expired", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getTokenType() != VerificationTokenType.ACCOUNT_ACTIVATION) {
            throw new ApiException("Invalid verification code type", HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        verificationToken.setUsed(true);

        userService.saveUser(user);
        tokenRepository.save(verificationToken);
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

        String verificationCode = generateVerificationCode();
        createVerificationToken(user, verificationCode, VerificationTokenType.EMAIL_CHANGE, request.newEmail());

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
        VerificationToken verificationToken = getVerificationTokenByCode(code);

        if (verificationToken.isUsed()) {
            throw new ApiException("Verification code already used", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Verification code expired", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getTokenType() != VerificationTokenType.EMAIL_CHANGE) {
            throw new ApiException("Invalid verification code type", HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        String newEmail = verificationToken.getAdditionalData();

        if (newEmail == null) {
            throw new ApiException("New email not found", HttpStatus.BAD_REQUEST);
        }

        user.setEmail(newEmail);
        userService.saveUser(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

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

        String verificationCode = generateVerificationCode();
        String encodedNewPassword = passwordEncoder.encode(request.newPassword());

        createVerificationToken(user, verificationCode, VerificationTokenType.PASSWORD_CHANGE, encodedNewPassword);

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
        VerificationToken verificationToken = getVerificationTokenByCode(code);

        if (verificationToken.isUsed()) {
            throw new ApiException("Verification code already used", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Verification code expired", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getTokenType() != VerificationTokenType.PASSWORD_CHANGE) {
            throw new ApiException("Invalid verification code type", HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        String newPasswordHash = verificationToken.getAdditionalData();

        if (newPasswordHash == null) {
            throw new ApiException("New password not found", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(newPasswordHash);
        userService.saveUser(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) throws MessagingException {
        User user = userService.findByEmailWithRoles(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        String verificationCode = generateVerificationCode();
        createVerificationToken(user, verificationCode, VerificationTokenType.PASSWORD_RESET, null);

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
        VerificationToken verificationToken = getVerificationTokenByCode(token);

        if (verificationToken.isUsed()) {
            throw new ApiException("Verification token already used", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Verification token expired", HttpStatus.BAD_REQUEST);
        }

        if (verificationToken.getTokenType() != VerificationTokenType.PASSWORD_RESET) {
            throw new ApiException("Invalid verification token type", HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        String newPasswordHash = passwordEncoder.encode(request.newPassword());

        user.setPassword(newPasswordHash);
        userService.saveUser(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        refreshTokenService.revokeAllUserTokens(user);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void createVerificationToken(User user, String token, VerificationTokenType tokenType, String additionalData) {
        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .tokenType(tokenType)
                .additionalData(additionalData)
                .build();

        tokenRepository.save(verificationToken);
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

    private VerificationToken getVerificationTokenByCode(String code) {
        return tokenRepository.findByToken(code)
                .orElseThrow(() -> new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST));
    }

    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
