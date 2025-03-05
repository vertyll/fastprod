package com.vertyll.fastprod.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.service.AuthService;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.common.exception.GlobalExceptionHandler;
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
import org.hamcrest.Matchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RegisterRequestDto registerRequest;
    private AuthRequestDto authRequest;
    private AuthResponseDto authResponse;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        registerRequest = RegisterRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        authRequest = AuthRequestDto.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        authResponse = AuthResponseDto.builder()
                .token("jwt-token")
                .type("Bearer")
                .build();
    }

    @Test
    void register_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(authService).register(any(RegisterRequestDto.class));

        // when & then
        mockMvc.perform(post("/auth/register")
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
        RegisterRequestDto invalidRequest = RegisterRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .password("password123")
                .build();

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).register(any(RegisterRequestDto.class));
    }

    @Test
    void register_WhenMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // given
        RegisterRequestDto invalidRequest = new RegisterRequestDto();

        // when & then
        mockMvc.perform(post("/auth/register")
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
                .when(authService).register(any(RegisterRequestDto.class));

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldReturnToken() throws Exception {
        // given
        when(authService.authenticate(any(AuthRequestDto.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/auth/authenticate")
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
        when(authService.authenticate(any(AuthRequestDto.class)))
                .thenThrow(new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        // when & then
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void authenticate_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // given
        AuthRequestDto invalidRequest = AuthRequestDto.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        // when & then
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).authenticate(any(AuthRequestDto.class));
    }

    @Test
    void authenticate_WhenMissingCredentials_ShouldReturnBadRequest() throws Exception {
        // given
        AuthRequestDto invalidRequest = new AuthRequestDto();

        // when & then
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(authService, never()).authenticate(any(AuthRequestDto.class));
    }

    @Test
    void verifyAccount_WhenValidCode_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(authService).verifyAccount(anyString());

        // when & then
        mockMvc.perform(post("/auth/verify")
                        .param("code", "123456"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified successfully"));

        verify(authService).verifyAccount("123456");
    }

    @Test
    void verifyAccount_WhenInvalidCode_ShouldReturnBadRequest() throws Exception {
        // given
        doThrow(new ApiException("Invalid verification code", HttpStatus.BAD_REQUEST))
                .when(authService).verifyAccount(anyString());

        // when & then
        mockMvc.perform(post("/auth/verify")
                        .param("code", "invalid"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid verification code"));
    }
}