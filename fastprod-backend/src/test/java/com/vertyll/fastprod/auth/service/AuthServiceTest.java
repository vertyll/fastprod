package com.vertyll.fastprod.auth.service;

import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.model.VerificationToken;
import com.vertyll.fastprod.auth.repository.VerificationTokenRepository;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.email.enums.EmailTemplateName;
import com.vertyll.fastprod.email.service.EmailService;
import com.vertyll.fastprod.role.model.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.model.User;
import com.vertyll.fastprod.user.repository.UserRepository;
import jakarta.mail.MessagingException;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<VerificationToken> tokenCaptor;

    private RegisterRequestDto registerRequest;
    private AuthRequestDto authRequest;
    private User user;
    private Role userRole;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        authRequest = new AuthRequestDto(
                "john@example.com",
                "password123"
        );

        userRole = Role.builder()
                .name("USER")
                .description("Default user role")
                .build();

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .roles(Set.of(userRole))
                .enabled(false)
                .build();

        verificationToken = VerificationToken.builder()
                .token("123456")
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
    }

    @Test
    void register_ShouldCreateNewUser() throws MessagingException {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        authService.register(registerRequest);

        // then
        verify(userRepository).save(userCaptor.capture());
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendEmail(
                eq("john@example.com"),
                eq("John"),
                eq(EmailTemplateName.ACTIVATE_ACCOUNT),
                anyString(),
                anyString()
        );

        User capturedUser = userCaptor.getValue();
        assertEquals("John", capturedUser.getFirstName());
        assertEquals("john@example.com", capturedUser.getEmail());
        assertFalse(capturedUser.isEnabled());
    }

    @Test
    void register_WhenEmailExists_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already registered", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void authenticate_ShouldReturnToken() {
        // given
        user.setEnabled(true);
        when(userRepository.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // when
        AuthResponseDto response = authService.authenticate(authRequest);

        // then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());
    }

    @Test
    void authenticate_WhenUserNotEnabled_ShouldThrowException() {
        // given
        user.setEnabled(false);
        when(userRepository.findByEmailWithRoles(anyString())).thenReturn(Optional.of(user));

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.authenticate(authRequest)
        );

        assertEquals("Account not verified", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void verifyAccount_ShouldActivateUser() {
        // given
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        // when
        authService.verifyAccount("123456");

        // then
        verify(userRepository).save(userCaptor.capture());
        verify(tokenRepository).save(tokenCaptor.capture());

        User verifiedUser = userCaptor.getValue();
        VerificationToken usedToken = tokenCaptor.getValue();

        assertTrue(verifiedUser.isEnabled());
        assertTrue(usedToken.isUsed());
    }

    @Test
    void verifyAccount_WhenTokenExpired_ShouldThrowException() {
        // given
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.verifyAccount("123456")
        );

        assertEquals("Verification code expired", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void verifyAccount_WhenTokenAlreadyUsed_ShouldThrowException() {
        // given
        verificationToken.setUsed(true);
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(verificationToken));

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.verifyAccount("123456")
        );

        assertEquals("Verification code already used", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
