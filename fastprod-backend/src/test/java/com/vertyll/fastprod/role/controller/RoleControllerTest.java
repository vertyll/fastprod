package com.vertyll.fastprod.role.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.common.exception.GlobalExceptionHandler;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.enums.RoleType;
import com.vertyll.fastprod.role.service.RoleService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RoleCreateDto createDto;
    private RoleResponseDto responseDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        createDto = new RoleCreateDto();
        createDto.setName("ADMIN");
        createDto.setDescription("Administrator role");

        responseDto = new RoleResponseDto();
        responseDto.setId(1L);
        responseDto.setName("ADMIN");
        responseDto.setDescription("Administrator role");
    }

    @Test
    void createRole_WhenValidInput_ShouldReturnCreated() throws Exception {
        // given
        when(roleService.createRole(any(RoleCreateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/roles")
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
        createDto.setName(null);

        // when & then
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(roleService, never()).createRole(any());
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
                .andExpect(jsonPath("$.data[0]").value(RoleType.values()[0].name()))
                .andExpect(jsonPath("$.message").value("Role types retrieved successfully"));
    }
}