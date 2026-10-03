package com.vertyll.fastprod.user.controller;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;
import com.vertyll.fastprod.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;
    private Jwt token;

    @SuppressWarnings("NullAway")
    @Mock
    private UserService userService;

    @SuppressWarnings("NullAway")
    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        token = Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject("kc-1")
            .claim("email", "jan@example.com")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        validator.close();
    }

    @Test
    void getCurrentUser_ShouldReturnTheSignedInAccount() throws Exception {
        when(userService.currentUser(token))
            .thenReturn(new UserResponseDto(1L, "Jan", "Kowalski", "jan@example.com", Set.of(RoleType.USER), true));

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("jan@example.com"))
            .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void updateProfile_WhenValid_ShouldReturnUpdatedAccount() throws Exception {
        when(userService.updateCurrentUserProfile(token, new ProfileUpdateDto("Janusz", "Nowak")))
            .thenReturn(new UserResponseDto(1L, "Janusz", "Nowak", "jan@example.com", Set.of(RoleType.USER), true));

        mockMvc
            .perform(
                put("/users/me/profile").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"Janusz\",\"lastName\":\"Nowak\"}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Janusz"));
    }

    @Test
    void updateProfile_WhenNameBlank_ShouldReturnBadRequest() throws Exception {
        mockMvc
            .perform(
                put("/users/me/profile").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"firstName\":\"\",\"lastName\":\"\"}")
            )
            .andExpect(status().isBadRequest());

        verify(userService, never()).updateCurrentUserProfile(any(), any());
    }
}
