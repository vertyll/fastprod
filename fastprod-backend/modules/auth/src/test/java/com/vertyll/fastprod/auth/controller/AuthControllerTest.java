package com.vertyll.fastprod.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.ChangeEmailRequestDto;
import com.vertyll.fastprod.auth.dto.ChangePasswordRequestDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.dto.ResendVerificationRequestDto;
import com.vertyll.fastprod.auth.dto.ResetPasswordRequestDto;
import com.vertyll.fastprod.auth.service.AuthService;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
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
        doNothing().when(authService).register(any(RegisterRequestDto.class));

        mockMvc
            .perform(
                post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto("John", "Doe", "invalid-email", "password123");

        mockMvc
            .perform(
                post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never()).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        RegisterRequestDto invalidRequest = new RegisterRequestDto("", "", "", "");

        mockMvc
            .perform(
                post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never()).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturnBadRequest() throws Exception {
        doThrow(new ApiException("Email already registered", HttpStatus.BAD_REQUEST)).when(authService)
            .register(any(RegisterRequestDto.class));

        mockMvc
            .perform(
                post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Email already registered"));
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldReturnToken() throws Exception {
        when(
            authService
                .authenticate(any(AuthRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class))
        ).thenReturn(authResponse);

        mockMvc
            .perform(
                post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authRequest))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void authenticate_WhenInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(
            authService
                .authenticate(any(AuthRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class))
        ).thenThrow(new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        mockMvc
            .perform(
                post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authRequest))
            )
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }

    @Test
    void authenticate_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("invalid-email", "password123", "web-browser");

        mockMvc
            .perform(
                post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never())
            .authenticate(any(AuthRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void authenticate_WhenMissingCredentials_ShouldReturnBadRequest() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("", "", "web-browser");

        mockMvc
            .perform(
                post("/auth/authenticate").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never())
            .authenticate(any(AuthRequestDto.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void refreshToken_WhenValidRefreshToken_ShouldReturnNewToken() throws Exception {
        when(authService.refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/auth/refresh-token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void refreshToken_WhenInvalidRefreshToken_ShouldReturnUnauthorized() throws Exception {
        when(authService.refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class)))
            .thenThrow(new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/refresh-token"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Invalid refresh token"));
    }

    @Test
    void logout_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/logout")).andDo(print()).andExpect(status().isNoContent());

        verify(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void logoutAll_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).logoutAllSessions(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/logout-all")).andDo(print()).andExpect(status().isNoContent());

        verify(authService).logoutAllSessions(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void verifyAccount_WhenValidCode_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).verifyAccount(anyString());

        mockMvc.perform(post("/auth/verify").param("code", "123456")).andDo(print()).andExpect(status().isNoContent());

        verify(authService).verifyAccount("123456");
    }

    @Test
    void verifyAccount_WhenInvalidCode_ShouldReturnBadRequest() throws Exception {
        doThrow(new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST)).when(authService)
            .verifyAccount(anyString());

        mockMvc.perform(post("/auth/verify").param("code", "invalid"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Invalid verification code"));
    }

    @Test
    void resendVerificationCode_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("john@example.com");
        doNothing().when(authService).resendVerificationCode(anyString());

        mockMvc
            .perform(
                post("/auth/resend-verification-code").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).resendVerificationCode("john@example.com");
    }

    @Test
    void resendVerificationCode_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("invalid-email");

        mockMvc
            .perform(
                post("/auth/resend-verification-code").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never()).resendVerificationCode(anyString());
    }

    @Test
    void resendVerificationCode_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("nonexistent@example.com");
        doThrow(new ApiException("User not found", HttpStatus.NOT_FOUND)).when(authService)
            .resendVerificationCode(anyString());

        mockMvc
            .perform(
                post("/auth/resend-verification-code").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void resendVerificationCode_WhenAccountAlreadyVerified_ShouldReturnBadRequest() throws Exception {
        ResendVerificationRequestDto request = new ResendVerificationRequestDto("john@example.com");
        doThrow(new ApiException("Account already verified", HttpStatus.BAD_REQUEST)).when(authService)
            .resendVerificationCode(anyString());

        mockMvc
            .perform(
                post("/auth/resend-verification-code").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Account already verified"));
    }

    @Test
    void requestEmailChange_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        mockMvc
            .perform(
                post("/auth/change-email-request").with(user("john@example.com").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(changeEmailRequest))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).requestEmailChange(any(ChangeEmailRequestDto.class));
    }

    @Test
    void requestEmailChange_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        ChangeEmailRequestDto invalidRequest = new ChangeEmailRequestDto("password123", "invalid-email");

        mockMvc
            .perform(
                post("/auth/change-email-request").with(user("john@example.com").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never()).requestEmailChange(any(ChangeEmailRequestDto.class));
    }

    @Test
    void verifyEmailChange_WhenValidCode_ShouldReturnSuccess() throws Exception {
        when(authService.verifyEmailChange(anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class)))
            .thenReturn(authResponse);

        mockMvc.perform(post("/auth/verify-email-change").param("code", "123456"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService)
            .verifyEmailChange(eq("123456"), any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void requestPasswordChange_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        mockMvc
            .perform(
                post("/auth/change-password-request").with(user("john@example.com").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(changePasswordRequest))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).requestPasswordChange(any(ChangePasswordRequestDto.class));
    }

    @Test
    void verifyPasswordChange_WhenValidCode_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).verifyPasswordChange(anyString());

        mockMvc.perform(post("/auth/verify-password-change").param("code", "123456"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).verifyPasswordChange("123456");
    }

    @Test
    void requestPasswordReset_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).sendPasswordResetEmail(anyString());

        mockMvc.perform(post("/auth/reset-password-request").param("email", "john@example.com"))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).sendPasswordResetEmail("john@example.com");
    }

    @Test
    void resetPassword_WhenValidTokenAndRequest_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).resetPassword(anyString(), any(ResetPasswordRequestDto.class));

        mockMvc
            .perform(
                post("/auth/reset-password").param("token", "valid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resetPasswordRequest))
            )
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(authService).resetPassword(eq("valid-token"), any(ResetPasswordRequestDto.class));
    }

    @Test
    void resetPassword_WhenInvalidPassword_ShouldReturnBadRequest() throws Exception {
        ResetPasswordRequestDto invalidRequest = new ResetPasswordRequestDto("");

        mockMvc
            .perform(
                post("/auth/reset-password").param("token", "valid-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"));

        verify(authService, never()).resetPassword(anyString(), any(ResetPasswordRequestDto.class));
    }
}
