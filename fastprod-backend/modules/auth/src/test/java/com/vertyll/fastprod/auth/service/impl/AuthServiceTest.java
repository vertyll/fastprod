package com.vertyll.fastprod.auth.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.ChangeEmailRequestDto;
import com.vertyll.fastprod.auth.dto.ChangePasswordRequestDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.dto.ResetPasswordRequestDto;
import com.vertyll.fastprod.auth.entity.VerificationToken;
import com.vertyll.fastprod.auth.enums.VerificationTokenType;
import com.vertyll.fastprod.auth.mapper.AuthMapper;
import com.vertyll.fastprod.auth.service.JwtService;
import com.vertyll.fastprod.auth.service.RefreshTokenService;
import com.vertyll.fastprod.auth.service.VerificationTokenService;
import com.vertyll.fastprod.email.enums.EmailTemplateName;
import com.vertyll.fastprod.email.service.EmailService;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.sharedinfrastructure.config.CookieProperties;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.service.UserService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressFBWarnings(
    value = {
        "URF_UNREAD_FIELD",
        "HARD_CODE_PASSWORD",
        "HTTPONLY_COOKIE",
        "INSECURE_COOKIE"
    },
    justification = "Test class: authMapper is used by Mockito injection; hardcoded test passwords and cookie configurations are safe in unit tests"
)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private VerificationTokenService verificationTokenService;

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
    private CookieProperties cookieProperties;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Spy

    @SuppressWarnings("UnusedVariable")

    private final AuthMapper authMapper = Mappers.getMapper(AuthMapper.class);

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

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

        userRole = Role.builder().name(RoleType.USER).description("Default user role").build();

        user = User.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .password("encodedPassword")
            .roles(Set.of(userRole))
            .verified(false)
            .build();

        verificationToken = VerificationToken.builder()
            .token("123456")
            .user(user)
            .expiryDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(24))
            .tokenType(VerificationTokenType.ACCOUNT_ACTIVATION)
            .build();
    }

    private VerificationToken tokenOf(VerificationTokenType type, @Nullable String additionalData) {
        return VerificationToken.builder()
            .token("123456")
            .user(user)
            .expiryDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(24))
            .tokenType(type)
            .additionalData(additionalData)
            .build();
    }

    private void setupCookieProperties() {
        lenient().when(cookieProperties.httpOnly()).thenReturn(true);
        lenient().when(cookieProperties.secure()).thenReturn(true);
        lenient().when(cookieProperties.path()).thenReturn("/");
        lenient().when(cookieProperties.sameSite()).thenReturn("Strict");
    }

    @Test
    void register_ShouldCreateNewUser() throws MessagingException {
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(any(RoleType.class))).thenReturn(userRole);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(verificationTokenService.createVerificationToken(any(User.class), any(VerificationTokenType.class), any()))
            .thenReturn("123456");

        authService.register(registerRequest);

        verify(userService).saveUser(userCaptor.capture());
        verify(verificationTokenService)
            .createVerificationToken(any(User.class), eq(VerificationTokenType.ACCOUNT_ACTIVATION), eq(null));
        verify(emailService).sendEmail(
            eq("john@example.com"),
            eq("John"),
            eq(EmailTemplateName.ACTIVATE_ACCOUNT),
            eq("123456"),
            anyString()
        );

        User capturedUser = userCaptor.getValue();
        assertEquals("John", capturedUser.getFirstName());
        assertEquals("john@example.com", capturedUser.getEmail());
        assertFalse(capturedUser.isVerified());
    }

    @Test
    void register_WhenEmailExists_ShouldThrowException() {
        when(userService.existsByEmail(anyString())).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> authService.register(registerRequest));
        assertEquals("Email already registered", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void authenticate_ShouldReturnToken() {
        setupCookieProperties();
        user.markVerified();
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604_800_000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class)))
            .thenReturn("refresh-token-jwt");

        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());
        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldThrowException() {
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
            ApiException.class,
            () -> authService.authenticate(authRequest, httpServletRequest, httpServletResponse)
        );
        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void authenticate_WhenUserNotVerified_ShouldThrowException() {
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

        ApiException exception = assertThrows(
            ApiException.class,
            () -> authService.authenticate(authRequest, httpServletRequest, httpServletResponse)
        );
        assertEquals("Account not verified", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void verifyAccount_ShouldActivateUser() {
        when(verificationTokenService.getValidToken("123456", VerificationTokenType.ACCOUNT_ACTIVATION))
            .thenReturn(verificationToken);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        authService.verifyAccount("123456");

        verify(userService).saveUser(userCaptor.capture());
        verify(verificationTokenService).markTokenAsUsed(verificationToken);

        User verifiedUser = userCaptor.getValue();
        assertTrue(verifiedUser.isVerified());
    }

    @Test
    void verifyAccount_WhenAccountAlreadyVerified_ShouldThrowException() {
        user.markVerified();
        when(verificationTokenService.getValidToken("123456", VerificationTokenType.ACCOUNT_ACTIVATION))
            .thenReturn(verificationToken);

        ApiException exception = assertThrows(ApiException.class, () -> authService.verifyAccount("123456"));
        assertEquals("Account already verified", exception.getMessage());
        verify(userService, never()).saveUser(any(User.class));
        verify(verificationTokenService, never()).markTokenAsUsed(any(VerificationToken.class));
    }

    @Test
    void resendVerificationCode_ShouldCreateNewTokenAndSendEmail() throws MessagingException {
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.createVerificationToken(any(User.class), any(VerificationTokenType.class), any()))
            .thenReturn("654321");

        authService.resendVerificationCode("john@example.com");

        verify(verificationTokenService).createVerificationToken(user, VerificationTokenType.ACCOUNT_ACTIVATION, null);
        verify(emailService)
            .sendEmail("john@example.com", "John", EmailTemplateName.ACTIVATE_ACCOUNT, "654321", "Account activation");
    }

    @Test
    void resendVerificationCode_WhenUserNotFound_ShouldThrowException() throws MessagingException {
        when(userService.findByEmailWithRoles("nonexistent@example.com")).thenReturn(Optional.empty());

        ApiException exception =
                assertThrows(ApiException.class, () -> authService.resendVerificationCode("nonexistent@example.com"));
        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(verificationTokenService, never()).createVerificationToken(any(), any(), any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void resendVerificationCode_WhenAccountAlreadyVerified_ShouldThrowException() throws MessagingException {
        user.markVerified();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));

        ApiException exception =
                assertThrows(ApiException.class, () -> authService.resendVerificationCode("john@example.com"));
        assertEquals("Account already verified", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(verificationTokenService, never()).createVerificationToken(any(), any(), any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void authenticate_ShouldCallAuthenticationManager() {
        setupCookieProperties();
        user.markVerified();
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604_800_000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class)))
            .thenReturn("refresh-token-jwt");

        authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        verify(authenticationManager)
            .authenticate(new UsernamePasswordAuthenticationToken("john@example.com", "password123"));
    }

    @Test
    void authenticate_ShouldCreateRefreshTokenWhenResponseNotNull() {
        setupCookieProperties();
        user.markVerified();
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604_800_000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class)))
            .thenReturn("refresh-token-jwt");

        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        verify(refreshTokenService).createRefreshToken(user, "web-browser", httpServletRequest);
        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());

        assertEquals("jwt-token", response.token());
    }

    @Test
    void authenticate_ShouldNotCreateRefreshTokenWhenResponseNull() {
        user.markVerified();
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");

        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, null);

        verify(refreshTokenService, never())
            .createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class));
        assertEquals("jwt-token", response.token());
    }

    @Test
    void authenticate_ShouldSetRefreshTokenCookie() {
        setupCookieProperties();
        user.markVerified();
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604_800_000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class)))
            .thenReturn("refresh-token-jwt");

        authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void authenticate_ShouldGenerateJwtToken() {
        setupCookieProperties();
        user.markVerified();
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("generated-jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604_800_000L);
        when(refreshTokenService.createRefreshToken(any(User.class), anyString(), any(HttpServletRequest.class)))
            .thenReturn("refresh-token-jwt");

        AuthResponseDto response = authService.authenticate(authRequest, httpServletRequest, httpServletResponse);

        verify(jwtService).generateToken(anyMap(), any(User.class));
        assertEquals("generated-jwt-token", response.token());
        assertEquals("Bearer", response.type());
    }

    @Test
    void refreshToken_ShouldReturnNewTokens() {
        setupCookieProperties();
        Cookie refreshCookie = new Cookie("refresh_token", "valid-refresh-token");
        when(httpServletRequest.getCookies()).thenReturn(
            new Cookie[] {
                refreshCookie
            }
        );
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(refreshTokenService.validateRefreshToken("valid-refresh-token")).thenReturn(user);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("new-access-token");
        when(refreshTokenService.rotateRefreshToken(anyString(), any(), any(HttpServletRequest.class)))
            .thenReturn("new-refresh-token");

        AuthResponseDto response = authService.refreshToken(httpServletRequest, httpServletResponse);

        verify(refreshTokenService).validateRefreshToken("valid-refresh-token");
        verify(jwtService).generateToken(anyMap(), any(User.class));
        verify(refreshTokenService).rotateRefreshToken(eq("valid-refresh-token"), isNull(), eq(httpServletRequest));
        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());
        assertEquals("new-access-token", response.token());
    }

    @Test
    void refreshToken_WhenNoRefreshToken_ShouldThrowException() {
        when(httpServletRequest.getCookies()).thenReturn(null);

        ApiException exception = assertThrows(
            ApiException.class,
            () -> authService.refreshToken(httpServletRequest, httpServletResponse)
        );
        assertEquals("Refresh token not found", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void logout_ShouldRevokeTokenAndDeleteCookie() {
        setupCookieProperties();
        Cookie refreshCookie = new Cookie("refresh_token", "valid-refresh-token");
        when(httpServletRequest.getCookies()).thenReturn(
            new Cookie[] {
                refreshCookie
            }
        );
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");

        authService.logout(httpServletRequest, httpServletResponse);

        verify(refreshTokenService).revokeRefreshToken("valid-refresh-token");
        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void logoutAllSessions_ShouldRevokeAllUserTokens() {
        setupCookieProperties();
        Cookie refreshCookie = new Cookie("refresh_token", "valid-refresh-token");
        when(httpServletRequest.getCookies()).thenReturn(
            new Cookie[] {
                refreshCookie
            }
        );
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(refreshTokenService.validateRefreshToken("valid-refresh-token")).thenReturn(user);

        authService.logoutAllSessions(httpServletRequest, httpServletResponse);

        verify(refreshTokenService).revokeAllUserTokens(user);
        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    void requestEmailChange_ShouldCreateVerificationToken() throws MessagingException {
        setupSecurityContext();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(true);
        when(userService.existsByEmail("newemail@example.com")).thenReturn(false);
        when(
            verificationTokenService
                .createVerificationToken(any(User.class), any(VerificationTokenType.class), anyString())
        ).thenReturn("123456");

        authService.requestEmailChange(changeEmailRequest);

        verify(verificationTokenService)
            .createVerificationToken(user, VerificationTokenType.EMAIL_CHANGE, "newemail@example.com");
        verify(emailService).sendEmail(
            "newemail@example.com",
            "John",
            EmailTemplateName.CHANGE_EMAIL,
            "123456",
            "Email Change Verification"
        );
    }

    @Test
    void requestEmailChange_WhenInvalidPassword_ShouldThrowException() {
        setupSecurityContext();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(false);

        ApiException exception =
                assertThrows(ApiException.class, () -> authService.requestEmailChange(changeEmailRequest));
        assertEquals("Invalid current password", exception.getMessage());
    }

    @Test
    void verifyEmailChange_ShouldUpdateEmailAndReturnTokens() {
        setupCookieProperties();
        verificationToken = tokenOf(VerificationTokenType.EMAIL_CHANGE, "newemail@example.com");
        when(verificationTokenService.getValidToken("123456", VerificationTokenType.EMAIL_CHANGE))
            .thenReturn(verificationToken);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("new-jwt-token");
        when(jwtService.getRefreshTokenCookieName()).thenReturn("refresh_token");
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(604_800_000L);
        when(refreshTokenService.createRefreshToken(any(User.class), isNull(), any(HttpServletRequest.class)))
            .thenReturn("new-refresh-token");

        AuthResponseDto response = authService.verifyEmailChange("123456", httpServletRequest, httpServletResponse);

        verify(userService).saveUser(userCaptor.capture());
        verify(verificationTokenService).markTokenAsUsed(verificationToken);
        verify(refreshTokenService).revokeAllUserTokens(user);
        verify(httpServletResponse).addHeader(eq("Set-Cookie"), anyString());

        User updatedUser = userCaptor.getValue();
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("new-jwt-token", response.token());
    }

    @Test
    void requestPasswordChange_ShouldCreateVerificationToken() throws MessagingException {
        setupSecurityContext();
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(
            verificationTokenService
                .createVerificationToken(any(User.class), any(VerificationTokenType.class), anyString())
        ).thenReturn("123456");

        authService.requestPasswordChange(changePasswordRequest);

        verify(verificationTokenService)
            .createVerificationToken(user, VerificationTokenType.PASSWORD_CHANGE, "encodedNewPassword");
        verify(emailService).sendEmail(
            "john@example.com",
            "John",
            EmailTemplateName.CHANGE_PASSWORD,
            "123456",
            "Password Change Verification"
        );
    }

    @Test
    void verifyPasswordChange_ShouldUpdatePassword() {
        verificationToken = tokenOf(VerificationTokenType.PASSWORD_CHANGE, "encodedNewPassword");
        when(verificationTokenService.getValidToken("123456", VerificationTokenType.PASSWORD_CHANGE))
            .thenReturn(verificationToken);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        authService.verifyPasswordChange("123456");

        verify(userService).saveUser(userCaptor.capture());
        verify(verificationTokenService).markTokenAsUsed(verificationToken);
        verify(refreshTokenService).revokeAllUserTokens(user);

        User updatedUser = userCaptor.getValue();
        assertEquals("encodedNewPassword", updatedUser.getPassword());
    }

    @Test
    void sendPasswordResetEmail_ShouldCreateTokenAndSendEmail() throws MessagingException {
        when(userService.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.createVerificationToken(any(User.class), any(VerificationTokenType.class), any()))
            .thenReturn("123456");

        authService.sendPasswordResetEmail("john@example.com");

        verify(verificationTokenService).createVerificationToken(user, VerificationTokenType.PASSWORD_RESET, null);
        verify(emailService)
            .sendEmail("john@example.com", "John", EmailTemplateName.RESET_PASSWORD, "123456", "Password Reset");
    }

    @Test
    void resetPassword_ShouldUpdatePassword() {
        verificationToken = tokenOf(VerificationTokenType.PASSWORD_RESET, null);
        when(verificationTokenService.getValidToken("valid-token", VerificationTokenType.PASSWORD_RESET))
            .thenReturn(verificationToken);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userService.saveUser(any(User.class))).thenReturn(user);

        authService.resetPassword("valid-token", resetPasswordRequest);

        verify(userService).saveUser(userCaptor.capture());
        verify(verificationTokenService).markTokenAsUsed(verificationToken);
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
