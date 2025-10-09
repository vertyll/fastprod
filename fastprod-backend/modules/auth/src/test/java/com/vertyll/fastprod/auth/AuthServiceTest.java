package com.vertyll.fastprod.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.email.EmailService;
import com.vertyll.fastprod.email.EmailTemplateName;
import com.vertyll.fastprod.role.Role;
import com.vertyll.fastprod.role.RoleService;
import com.vertyll.fastprod.user.User;
import com.vertyll.fastprod.user.UserService;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;

    @Mock private VerificationTokenRepository tokenRepository;

    @Mock private RoleService roleService;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtService jwtService;

    @Mock private AuthenticationManager authenticationManager;

    @Mock private EmailService emailService;

    @InjectMocks private AuthService authService;

    @Captor private ArgumentCaptor<User> userCaptor;

    @Captor private ArgumentCaptor<VerificationToken> tokenCaptor;

    private RegisterRequestDto registerRequest;
    private AuthRequestDto authRequest;
    private User user;
    private Role userRole;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto("John", "Doe", "john@example.com", "password123");

        authRequest = new AuthRequestDto("john@example.com", "password123");

        userRole = Role.builder().name("USER").description("Default user role").build();

        user =
                User.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .password("encodedPassword")
                        .roles(Set.of(userRole))
                        .enabled(false)
                        .build();

        verificationToken =
                VerificationToken.builder()
                        .token("123456")
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusHours(24))
                        .used(false)
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
        assertFalse(capturedUser.isEnabled());
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
        user.setEnabled(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // when
        AuthResponseDto response = authService.authenticate(authRequest);

        // then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldThrowException() {
        // given
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.authenticate(authRequest));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void authenticate_WhenUserNotEnabled_ShouldThrowException() {
        // given
        user.setEnabled(false);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> authService.authenticate(authRequest));

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

        assertTrue(verifiedUser.isEnabled());
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
                        anyString(), // verification code
                        eq("Account activation"));
    }

    @Test
    void authenticate_ShouldCallAuthenticationManager() {
        // given
        user.setEnabled(true);
        when(userService.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // when
        authService.authenticate(authRequest);

        // then
        verify(authenticationManager)
                .authenticate(
                        new UsernamePasswordAuthenticationToken("john@example.com", "password123"));
    }
}
