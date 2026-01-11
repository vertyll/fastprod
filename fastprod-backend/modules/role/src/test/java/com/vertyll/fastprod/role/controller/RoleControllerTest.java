package com.vertyll.fastprod.role.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
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

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.common.exception.GlobalExceptionHandler;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.service.RoleService;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @SuppressWarnings("NullAway")
    @Mock
    private RoleService roleService;

    @SuppressWarnings("NullAway")
    @InjectMocks
    private RoleController roleController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RoleCreateDto createDto;
    private RoleResponseDto responseDto;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(roleController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setValidator(validator)
                        .build();

        createDto = new RoleCreateDto("ADMIN", "Administrator role");

        responseDto = new RoleResponseDto(1L, "ADMIN", "Administrator role");
    }

    @AfterEach
    void tearDown() {
        if (validator != null) {
            validator.close();
        }
    }

    @Test
    void createRole_WhenValidInput_ShouldReturnCreated() throws Exception {
        // given
        when(roleService.createRole(any(RoleCreateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        post("/roles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("ADMIN"))
                .andExpect(jsonPath("$.data.description").value("Administrator role"))
                .andExpect(jsonPath("$.message").value("Role created successfully"));
    }

    @Test
    void createRole_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        // given
        @SuppressWarnings("NullAway")
        RoleCreateDto invalidCreateDto = new RoleCreateDto(null, "Administrator role");

        // when & then
        mockMvc.perform(
                        post("/roles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidCreateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(roleService, never()).createRole(any());
    }

    @Test
    void updateRole_WhenValidInput_ShouldReturnUpdated() throws Exception {
        // given
        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Administrator role");

        when(roleService.updateRole(anyLong(), any(RoleUpdateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        put("/roles/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("ADMIN"))
                .andExpect(jsonPath("$.data.description").value("Administrator role"))
                .andExpect(jsonPath("$.message").value("Role updated successfully"));
    }

    @Test
    void updateRole_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // given
        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Administrator role");

        when(roleService.updateRole(anyLong(), any(RoleUpdateDto.class)))
                .thenThrow(new ApiException("Role not found", HttpStatus.NOT_FOUND));

        // when & then
        mockMvc.perform(
                        put("/roles/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found"));
    }

    @Test
    void updateRole_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        // given
        @SuppressWarnings("NullAway")
        RoleUpdateDto invalidUpdateDto = new RoleUpdateDto(null, "Administrator role");

        // when & then
        mockMvc.perform(
                        put("/roles/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(roleService, never()).updateRole(anyLong(), any());
    }

    @Test
    void getRole_WhenExists_ShouldReturnRole() throws Exception {
        // given
        when(roleService.getRoleById(1L)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/roles/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("ADMIN"))
                .andExpect(jsonPath("$.message").value("Role retrieved successfully"));
    }

    @Test
    void getRole_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // given
        when(roleService.getRoleById(1L))
                .thenThrow(new ApiException("Role not found", HttpStatus.NOT_FOUND));

        // when & then
        mockMvc.perform(get("/roles/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found"));
    }

    @Test
    void getAllRoleTypes_ShouldReturnAllTypes() throws Exception {
        mockMvc.perform(get("/roles/types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[?(@=='ADMIN')]").exists())
                .andExpect(jsonPath("$.message").value("Role types retrieved successfully"));
    }
}
