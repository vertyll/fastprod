package com.vertyll.fastprod.role.controller;

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

import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
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
        when(roleService.createRole(any(RoleCreateDto.class))).thenReturn(responseDto);

        mockMvc
            .perform(
                post("/roles").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto))
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("ADMIN"))
            .andExpect(jsonPath("$.description").value("Administrator role"));
    }

    @Test
    void createRole_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        @SuppressWarnings("NullAway") RoleCreateDto invalidCreateDto = new RoleCreateDto(null, "Administrator role");

        mockMvc
            .perform(
                post("/roles").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidCreateDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("errors.common.validationFailed"));

        verify(roleService, never()).createRole(any());
    }

    @Test
    void updateRole_WhenValidInput_ShouldReturnUpdated() throws Exception {
        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Administrator role");

        when(roleService.updateRole(anyLong(), any(RoleUpdateDto.class))).thenReturn(responseDto);

        mockMvc
            .perform(
                put("/roles/1").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("ADMIN"))
            .andExpect(jsonPath("$.description").value("Administrator role"));
    }

    @Test
    void updateRole_WhenNotFound_ShouldReturnNotFound() throws Exception {
        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Administrator role");

        when(roleService.updateRole(anyLong(), any(RoleUpdateDto.class)))
            .thenThrow(new ApiException("errors.role.notFound", HttpStatus.NOT_FOUND));

        mockMvc
            .perform(
                put("/roles/1").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("errors.role.notFound"));
    }

    @Test
    void updateRole_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        @SuppressWarnings("NullAway") RoleUpdateDto invalidUpdateDto = new RoleUpdateDto(null, "Administrator role");

        mockMvc
            .perform(
                put("/roles/1").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdateDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("errors.common.validationFailed"));

        verify(roleService, never()).updateRole(anyLong(), any());
    }

    @Test
    void getRole_WhenExists_ShouldReturnRole() throws Exception {
        when(roleService.getRoleById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/roles/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void getRole_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(roleService.getRoleById(1L)).thenThrow(new ApiException("errors.role.notFound", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/roles/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("errors.role.notFound"));
    }

    @Test
    void getAllRoleTypes_ShouldReturnAllTypes() throws Exception {
        mockMvc.perform(get("/roles/types"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[?(@=='ADMIN')]").exists());
    }
}
