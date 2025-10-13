package com.vertyll.fastprod.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.common.exception.GlobalExceptionHandler;
import java.util.Set;

import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.service.UserService;
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

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock private UserService userService;

    @InjectMocks private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserCreateDto createDto;
    private UserUpdateDto updateDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(userController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setValidator(validator)
                        .build();

        createDto =
                new UserCreateDto("John", "Doe", "john@example.com", "password123", Set.of("USER"));

        updateDto =
                new UserUpdateDto(
                        "John Updated",
                        "Doe Updated",
                        "john.updated@example.com",
                        null, // password can be null for updates
                        Set.of("USER", "ADMIN"));

        responseDto =
                new UserResponseDto(1L, "John", "Doe", "john@example.com", Set.of("USER"), true);
    }

    @Test
    void createUser_WhenValidInput_ShouldReturnCreated() throws Exception {
        // given
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(userService).createUser(any(UserCreateDto.class));
    }

    @Test
    void createUser_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        // given
        UserCreateDto invalidCreateDto =
                new UserCreateDto(
                        "John",
                        "Doe",
                        "invalid-email", // invalid email format
                        "password123",
                        Set.of("USER"));

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidCreateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(userService, never()).createUser(any(UserCreateDto.class));
    }

    @Test
    void updateUser_WhenValidInput_ShouldReturnSuccess() throws Exception {
        // given
        when(userService.updateUser(anyLong(), any(UserUpdateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        put("/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        verify(userService).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        // given
        doThrow(new ApiException("User not found", HttpStatus.NOT_FOUND))
                .when(userService)
                .updateUser(anyLong(), any(UserUpdateDto.class));

        // when & then
        mockMvc.perform(
                        put("/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUser_WhenExists_ShouldReturnUser() throws Exception {
        // given
        when(userService.getUserById(1L)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUser_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // given
        when(userService.getUserById(1L))
                .thenThrow(new ApiException("User not found", HttpStatus.NOT_FOUND));

        // when & then
        mockMvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
