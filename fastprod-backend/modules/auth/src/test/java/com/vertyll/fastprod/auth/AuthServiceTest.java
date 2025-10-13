package com.vertyll.fastprod.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.vertyll.fastprod.auth.dto.*;
import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.auth.repository.VerificationTokenRepository;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.email.EmailService;
import com.vertyll.fastprod.email.enums.EmailTemplateName;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<VerificationToken> tokenCaptor;

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    private RegisterRequestDto registerRequest;
    private AuthRequestDto authRequest;
    private ChangeEmailRequestDto changeEmailRequest;
    private ChangePasswordRequestDto changePasswordRequest;
    private ResetPasswordRequestDto resetPasswordRequest;
    private User user;
    private Role userRole;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto("John", "Doe", "john@example.com", "password123");

        authRequest = new AuthRequestDto("john@example.com", "password123", "web-browser");

        changeEmailRequest = new ChangeEmailRequestDto("currentPassword123", "newemail@example.com");

        changePasswordRequest = new ChangePasswordRequestDto("currentPassword123", "newPassword123");

        resetPasswordRequest = new ResetPasswordRequestDto("newPassword123");

        userRole = Role.builder().name("USER").description("Default user role").build();

        user =
                User.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .password("encodedPassword")
                        .roles(Set.of(userRole))
                        .isVerified(false)
                        .build();

        verificationToken =
                VerificationToken.builder()
                        .token("123456")
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusHours(24))
                        .isUsed(false)
                        .tokenType(VerificationTokenType.ACCOUNT_ACTIVATION)
                        .build();
    }

    @Test
    void register_ShouldCreateNewUser() throws MessagingException {
        // given
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(userRole);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        authService.register(registerRequest);

        // then
        verify(userService).saveUser(userCaptor.capture());
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService)
                .sendEmail(
                        eq("john@example.com"),
                        eq("John"),
                        eq(EmailTemplateName.ACTIVATE_ACCOUNT),
                        anyString(),
                        anyString());

        User capturedUser = userCaptor.getValue();
        assertEquals("John", capturedUser.getFirstName());
        assertEquals("john@example.com", capturedUser.getEmail());
        assertFalse(capturedUser.isVerified());
    }

    @Test
    void register_WhenEmailExists_ShouldThrowException() {
        // given
        when(userService.existsByEmail(anyString())).thenReturn(true);

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.register(registerRequest));

        assertEquals("Email already registered", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void authenticate_ShouldReturnToken() {
        // given
        user.setVerified(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604800000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class))).thenReturn("refresh-token-jwt");

        // when
        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        // then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(httpServletResponse).addCookie(any(Cookie.class));
        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldThrowException() {
        // given
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.authenticate(authRequest, httpServletRequest, httpServletResponse));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void authenticate_WhenUserNotVerified_ShouldThrowException() {
        // given
        user.setVerified(false);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.authenticate(authRequest, httpServletRequest, httpServletResponse));

        assertEquals("Account not verified", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void verifyAccount_ShouldActivateUser() {
        // given
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        authService.verifyAccount("123456");

        // then
        verify(userService).saveUser(userCaptor.capture());
        verify(tokenRepository).save(tokenCaptor.capture());

        User verifiedUser = userCaptor.getValue();
        VerificationToken usedToken = tokenCaptor.getValue();

        assertTrue(verifiedUser.isVerified());
        assertTrue(usedToken.isUsed());
    }

    @Test
    void verifyAccount_WhenTokenNotFound_ShouldThrowException() {
        // given
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.verifyAccount("invalid-token"));

        assertEquals("Invalid verification code", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void verifyAccount_WhenTokenExpired_ShouldThrowException() {
        // given
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.verifyAccount("123456"));

        assertEquals("Verification code expired", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void verifyAccount_WhenTokenAlreadyUsed_ShouldThrowException() {
        // given
        verificationToken.setUsed(true);
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.verifyAccount("123456"));

        assertEquals("Verification code already used", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void verifyAccount_WhenWrongTokenType_ShouldThrowException() {
        // given
        verificationToken.setTokenType(VerificationTokenType.PASSWORD_RESET);
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.verifyAccount("123456"));

        assertEquals("Invalid verification code type", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void register_ShouldGenerateVerificationToken() throws MessagingException {
        // given
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(userRole);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        authService.register(registerRequest);

        // then
        verify(tokenRepository).save(tokenCaptor.capture());
        VerificationToken capturedToken = tokenCaptor.getValue();

        assertNotNull(capturedToken.getToken());
        assertEquals(6, capturedToken.getToken().length());
        assertNotNull(capturedToken.getUser());
        assertFalse(capturedToken.isUsed());
        assertTrue(capturedToken.getExpiryDate().isAfter(LocalDateTime.now()));
        assertEquals(VerificationTokenType.ACCOUNT_ACTIVATION, capturedToken.getTokenType());
    }

    @Test
    void register_ShouldSendActivationEmail() throws MessagingException {
        // given
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(userRole);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        authService.register(registerRequest);

        // then
        verify(emailService)
                .sendEmail(
                        eq("john@example.com"),
                        eq("John"),
                        eq(EmailTemplateName.ACTIVATE_ACCOUNT),
                        anyString(),
                        eq("Account activation"));
    }

    @Test
    void authenticate_ShouldCallAuthenticationManager() {
        // given
        user.setVerified(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604800000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class))).thenReturn("refresh-token-jwt");

        // when
        authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        // then
        verify(authenticationManager)
                .authenticate(
                        new UsernamePasswordAuthenticationToken("john@example.com", "password123"));
    }

    @Test
    void authenticate_ShouldCreateRefreshTokenWhenResponseNotNull() {
        // given
        user.setVerified(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604800000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class))).thenReturn("refresh-token-jwt");

        // when
        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        // then
        verify(refreshTokenService).createRefreshToken(user, "web-browser", httpServletRequest);
        verify(httpServletResponse).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("refresh_token", capturedCookie.getName());
        assertEquals("refresh-token-jwt", capturedCookie.getValue());
        assertTrue(capturedCookie.isHttpOnly());
        assertTrue(capturedCookie.getSecure());
        assertEquals("/", capturedCookie.getPath());

        assertEquals("jwt-token", response.token());
    }

    @Test
    void authenticate_ShouldNotCreateRefreshTokenWhenResponseNull() {
        // given
        user.setVerified(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // when
        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, null);

        // then
        verify(refreshTokenService, never()).createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class));
        assertEquals("jwt-token", response.token());
    }

    @Test
    void authenticate_ShouldSetRefreshTokenCookie() {
        // given
        user.setVerified(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604800000L); // 7 days
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class))).thenReturn("refresh-token-jwt");

        // when
        authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        // then
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token-jwt", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(604800, cookie.getMaxAge()); // 7 days in seconds
    }

    @Test
    void authenticate_ShouldGenerateJwtToken() {
        // given
        user.setVerified(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("generated-jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604800000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class))).thenReturn("refresh-token-jwt");

        // when
        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        // then
        verify(jwtService).generateToken(user);
        assertEquals("generated-jwt-token", response.token());
        assertEquals("Bearer", response.type());
    }

    // New tests for additional methods

    @Test
    void refreshToken_ShouldReturnNewTokens() {
        // given
        Cookie refreshCookie = new Cookie("refresh_token", "valid-refresh-token");
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(refreshTokenService.validateRefreshToken("valid-refresh-token")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.rotateRefreshToken(anyString(), any(), any(HttpServletRequest.class))).thenReturn("new-refresh-token");

        // when
        AuthResponseDto response = authService.refreshToken(httpServletRequest, httpServletResponse);

        // then
        verify(refreshTokenService).validateRefreshToken("valid-refresh-token");
        verify(jwtService).generateToken(user);
        verify(refreshTokenService).rotateRefreshToken("valid-refresh-token", null, httpServletRequest);
        verify(httpServletResponse).addCookie(any(Cookie.class));
        assertEquals("new-access-token", response.token());
    }

    @Test
    void refreshToken_WhenNoRefreshToken_ShouldThrowException() {
        // given
        when(httpServletRequest.getCookies()).thenReturn(null);

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> authService.refreshToken(httpServletRequest, httpServletResponse));
        assertEquals("Refresh token not found", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void logout_ShouldRevokeTokenAndDeleteCookie() {
        // given
        Cookie refreshCookie = new Cookie("refresh_token", "valid-refresh-token");
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");

        // when
        authService.logout(httpServletRequest, httpServletResponse);

        // then
        verify(refreshTokenService).revokeRefreshToken("valid-refresh-token");
        verify(httpServletResponse).addCookie(cookieCaptor.capture());

        Cookie deletedCookie = cookieCaptor.getValue();
        assertEquals("refresh_token", deletedCookie.getName());
        assertEquals("", deletedCookie.getValue());
        assertEquals(0, deletedCookie.getMaxAge());
    }

    @Test
    void logoutAllSessions_ShouldRevokeAllUserTokens() {
        // given
        Cookie refreshCookie = new Cookie("refresh_token", "valid-refresh-token");
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(refreshTokenService.validateRefreshToken("valid-refresh-token")).thenReturn(user);

        // when
        authService.logoutAllSessions(httpServletRequest, httpServletResponse);

        // then
        verify(refreshTokenService).revokeAllUserTokens(user);
        verify(httpServletResponse).addCookie(any(Cookie.class));
    }

    @Test
    void requestEmailChange_ShouldCreateVerificationToken() throws MessagingException {
        // given
        setupSecurityContext();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(true);
        when(userService.existsByEmail("newemail@example.com")).thenReturn(false);

        // when
        authService.requestEmailChange(changeEmailRequest);

        // then
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendEmail(
                eq("newemail@example.com"),
                eq("John"),
                eq(EmailTemplateName.CHANGE_EMAIL),
                anyString(),
                eq("Email Change Verification")
        );

        VerificationToken token = tokenCaptor.getValue();
        assertEquals(VerificationTokenType.EMAIL_CHANGE, token.getTokenType());
        assertEquals("newemail@example.com", token.getAdditionalData());
    }

    @Test
    void requestEmailChange_WhenInvalidPassword_ShouldThrowException() {
        // given
        setupSecurityContext();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(false);

        // when & then
        ApiException exception = assertThrows(ApiException.class,
                () -> authService.requestEmailChange(changeEmailRequest));
        assertEquals("Invalid current password", exception.getMessage());
    }

    @Test
    void verifyEmailChange_ShouldUpdateEmailAndReturnTokens() {
        // given
        verificationToken.setTokenType(VerificationTokenType.EMAIL_CHANGE);
        verificationToken.setAdditionalData("newemail@example.com");
        when(tokenRepository.findByToken("123456")).thenReturn(Optional.of(verificationToken));
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("new-jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604800000L);
        when(refreshTokenService.createRefreshToken(any(User.class), eq(null), any(HttpServletRequest.class))).thenReturn("new-refresh-token");

        // when
        AuthResponseDto response = authService.verifyEmailChange("123456", httpServletRequest, httpServletResponse);

        // then
        verify(userService).saveUser(userCaptor.capture());
        verify(refreshTokenService).revokeAllUserTokens(user);
        verify(httpServletResponse).addCookie(any(Cookie.class));

        User updatedUser = userCaptor.getValue();
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("new-jwt-token", response.token());
    }

    @Test
    void requestPasswordChange_ShouldCreateVerificationToken() throws MessagingException {
        // given
        setupSecurityContext();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // when
        authService.requestPasswordChange(changePasswordRequest);

        // then
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendEmail(
                eq("john@example.com"),
                eq("John"),
                eq(EmailTemplateName.CHANGE_PASSWORD),
                anyString(),
                eq("Password Change Verification")
        );

        VerificationToken token = tokenCaptor.getValue();
        assertEquals(VerificationTokenType.PASSWORD_CHANGE, token.getTokenType());
        assertEquals("encodedNewPassword", token.getAdditionalData());
    }

    @Test
    void verifyPasswordChange_ShouldUpdatePassword() {
        // given
        verificationToken.setTokenType(VerificationTokenType.PASSWORD_CHANGE);
        verificationToken.setAdditionalData("encodedNewPassword");
        when(tokenRepository.findByToken("123456")).thenReturn(Optional.of(verificationToken));
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        authService.verifyPasswordChange("123456");

        // then
        verify(userService).saveUser(userCaptor.capture());
        verify(refreshTokenService).revokeAllUserTokens(user);

        User updatedUser = userCaptor.getValue();
        assertEquals("encodedNewPassword", updatedUser.getPassword());
    }

    @Test
    void sendPasswordResetEmail_ShouldCreateTokenAndSendEmail() throws MessagingException {
        // given
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));

        // when
        authService.sendPasswordResetEmail("john@example.com");

        // then
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendEmail(
                eq("john@example.com"),
                eq("John"),
                eq(EmailTemplateName.RESET_PASSWORD),
                anyString(),
                eq("Password Reset")
        );

        VerificationToken token = tokenCaptor.getValue();
        assertEquals(VerificationTokenType.PASSWORD_RESET, token.getTokenType());
    }

    @Test
    void resetPassword_ShouldUpdatePassword() {
        // given
        verificationToken.setTokenType(VerificationTokenType.PASSWORD_RESET);
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(verificationToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        authService.resetPassword("valid-token", resetPasswordRequest);

        // then
        verify(userService).saveUser(userCaptor.capture());
        verify(refreshTokenService).revokeAllUserTokens(user);

        User updatedUser = userCaptor.getValue();
        assertEquals("encodedNewPassword", updatedUser.getPassword());
    }

    private void setupSecurityContext() {
        when(authentication.getName()).thenReturn("john@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
