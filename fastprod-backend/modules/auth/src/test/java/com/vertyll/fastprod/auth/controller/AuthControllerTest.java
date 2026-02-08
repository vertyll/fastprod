package com.vertyll.fastprod.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.vertyll.fastprod.auth.dto.*;
import com.vertyll.fastprod.auth.service.AuthService;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @SuppressWarnings("NullAway")
    @Mock
    private AuthService authService;

    @SuppressWarnings("NullAway")
    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RegisterRequestDto registerRequest;
    private AuthRequestDto authRequest;
    private AuthResponseDto authResponse;
    private ChangeEmailRequestDto changeEmailRequest;
    private ChangePasswordRequestDto changePasswordRequest;
    private ResetPasswordRequestDto resetPasswordRequest;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(authController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setValidator(validator)
                        .build();

        registerRequest = new RegisterRequestDto("John", "Doe", "john@example.com", "password123");

        authRequest = new AuthRequestDto("john@example.com", "password123", "web-browser");

        authResponse = new AuthResponseDto("jwt-token", "Bearer");

        changeEmailRequest = new ChangeEmailRequestDto("password123", "john.new@example.com");

        changePasswordRequest = new ChangePasswordRequestDto("oldPassword123", "newPassword123");

        resetPasswordRequest = new ResetPasswordRequestDto("newPassword123");
    }

    @AfterEach
    void tearDown() {
        if (validator != null) {
            validator.close();
        }
    }

    @Test
    void register_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(authService).register(any(RegisterRequestDto.class));

        // when & then
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // given
        RegisterRequestDto invalidRequest =
                new RegisterRequestDto("John", "Doe", "invalid-email", "password123");

        // when & then
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto("", "", "", "");

        // when & then
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturnBadRequest() throws Exception {
        // given
        doThrow(new ApiException("Email already registered", HttpStatus.BAD_REQUEST))
                .when(authService)
                .register(any(RegisterRequestDto.class));

        // when & then
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldReturnToken() throws Exception {
        // given
        when(authService.authenticate(
                        any(AuthRequestDto.class),
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class)))
                .thenReturn(authResponse);

        // when & then
        mockMvc.perform(
                        post("/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.message").value("Authentication successful"));
    }

    @Test
    void authenticate_WhenInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // given
        when(authService.authenticate(
                        any(AuthRequestDto.class),
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class)))
                .thenThrow(new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        // when & then
        mockMvc.perform(
                        post("/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void authenticate_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // given
        AuthRequestDto invalidRequest =
                new AuthRequestDto("invalid-email", "password123", "web-browser");

        // when & then
        mockMvc.perform(
                        post("/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never())
                .authenticate(
                        any(AuthRequestDto.class),
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class));
    }

    @Test
    void authenticate_WhenMissingCredentials_ShouldReturnBadRequest() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("", "", "web-browser");

        // when & then
        mockMvc.perform(
                        post("/auth/authenticate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never())
                .authenticate(
                        any(AuthRequestDto.class),
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class));
    }

    @Test
    void refreshToken_WhenValidRefreshToken_ShouldReturnNewToken() throws Exception {
        // given
        when(authService.refreshToken(
                        any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/auth/refresh-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"));
    }

    @Test
    void refreshToken_WhenInvalidRefreshToken_ShouldReturnUnauthorized() throws Exception {
        // given
        when(authService.refreshToken(
                        any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenThrow(new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        // when & then
        mockMvc.perform(post("/auth/refresh-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void logout_ShouldReturnSuccess() throws Exception {
        // given
        doNothing()
                .when(authService)
                .logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(post("/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void logoutAll_ShouldReturnSuccess() throws Exception {
        // given
        doNothing()
                .when(authService)
                .logoutAllSessions(any(HttpServletRequest.class), any(HttpServletResponse.class));

        // when & then
        mockMvc.perform(post("/auth/logout-all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message").value("Logged out from all sessions successfully"));

        verify(authService)
                .logoutAllSessions(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void verifyAccount_WhenValidCode_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(authService).verifyAccount(anyString());

        // when & then
        mockMvc.perform(post("/auth/verify").param("code", "123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified successfully"));

        verify(authService).verifyAccount("123456");
    }

    @Test
    void verifyAccount_WhenInvalidCode_ShouldReturnBadRequest() throws Exception {
        // given
        doThrow(new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST))
                .when(authService)
                .verifyAccount(anyString());

        // when & then
        mockMvc.perform(post("/auth/verify").param("code", "invalid"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid verification code"));
    }

    @Test
    void resendVerificationCode_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        // given
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("john@example.com");
        doNothing().when(authService).resendVerificationCode(anyString());

        // when & then
        mockMvc.perform(
                        post("/auth/resend-verification-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code sent successfully"));

        verify(authService).resendVerificationCode("john@example.com");
    }

    @Test
    void resendVerificationCode_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // given
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("invalid-email");

        // when & then
        mockMvc.perform(
                        post("/auth/resend-verification-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).resendVerificationCode(anyString());
    }

    @Test
    void resendVerificationCode_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        // given
        ResendVerificationRequestDto request =
                new ResendVerificationRequestDto("nonexistent@example.com");
        doThrow(new ApiException("User not found", HttpStatus.NOT_FOUND))
                .when(authService)
                .resendVerificationCode(anyString());

        // when & then
        mockMvc.perform(
                        post("/auth/resend-verification-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void resendVerificationCode_WhenAccountAlreadyVerified_ShouldReturnBadRequest()
            throws Exception {
        // given
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("john@example.com");
        doThrow(new ApiException("Account already verified", HttpStatus.BAD_REQUEST))
                .when(authService)
                .resendVerificationCode(anyString());

        // when & then
        mockMvc.perform(
                        post("/auth/resend-verification-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account already verified"));
    }

    @Test
    void requestEmailChange_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // given

        // when & then
        mockMvc.perform(
                        post("/auth/change-email-request")
                                .with(user("john@example.com").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changeEmailRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message").value("Email change verification sent to new email"));

        verify(authService).requestEmailChange(any(ChangeEmailRequestDto.class));
    }

    @Test
    void requestEmailChange_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // given
        ChangeEmailRequestDto invalidRequest =
                new ChangeEmailRequestDto("password123", "invalid-email");

        // when & then
        mockMvc.perform(
                        post("/auth/change-email-request")
                                .with(user("john@example.com").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).requestEmailChange(any(ChangeEmailRequestDto.class));
    }

    @Test
    void verifyEmailChange_WhenValidCode_ShouldReturnSuccess() throws Exception {
        // given
        when(authService.verifyEmailChange(
                        anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/auth/verify-email-change").param("code", "123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email changed successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));

        verify(authService)
                .verifyEmailChange(
                        eq("123456"),
                        any(HttpServletRequest.class),
                        any(HttpServletResponse.class));
    }

    @Test
    void requestPasswordChange_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // given

        // when & then
        mockMvc.perform(
                        post("/auth/change-password-request")
                                .with(user("john@example.com").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message").value("Password change verification sent to email"));

        verify(authService).requestPasswordChange(any(ChangePasswordRequestDto.class));
    }

    @Test
    void verifyPasswordChange_WhenValidCode_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(authService).verifyPasswordChange(anyString());

        // when & then
        mockMvc.perform(post("/auth/verify-password-change").param("code", "123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(authService).verifyPasswordChange("123456");
    }

    @Test
    void requestPasswordReset_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(authService).sendPasswordResetEmail(anyString());

        // when & then
        mockMvc.perform(post("/auth/reset-password-request").param("email", "john@example.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.message").value("Password reset instructions sent to email"));

        verify(authService).sendPasswordResetEmail("john@example.com");
    }

    @Test
    void resetPassword_WhenValidTokenAndRequest_ShouldReturnSuccess() throws Exception {
        // given
        doNothing()
                .when(authService)
                .resetPassword(anyString(), any(ResetPasswordRequestDto.class));

        // when & then
        mockMvc.perform(
                        post("/auth/reset-password")
                                .param("token", "valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(authService).resetPassword(eq("valid-token"), any(ResetPasswordRequestDto.class));
    }

    @Test
    void resetPassword_WhenInvalidPassword_ShouldReturnBadRequest() throws Exception {
        // given
        ResetPasswordRequestDto invalidRequest = new ResetPasswordRequestDto("");

        // when & then
        mockMvc.perform(
                        post("/auth/reset-password")
                                .param("token", "valid-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).resetPassword(anyString(), any(ResetPasswordRequestDto.class));
    }
}
