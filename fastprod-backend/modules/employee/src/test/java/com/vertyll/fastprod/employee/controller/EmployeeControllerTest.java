package com.vertyll.fastprod.employee.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import tools.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.common.exception.GlobalExceptionHandler;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.service.EmployeeService;
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

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private EmployeeCreateDto createDto;
    private EmployeeUpdateDto updateDto;
    private EmployeeResponseDto responseDto;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders.standaloneSetup(employeeController)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .setValidator(validator)
                        .build();

        createDto =
                new EmployeeCreateDto(
                        "John",
                        "Doe",
                        "john@example.com",
                        "password123",
                        Set.of("EMPLOYEE"));

        updateDto =
                new EmployeeUpdateDto(
                        "John Updated",
                        "Doe Updated",
                        "john.updated@example.com",
                        null,
                        Set.of("EMPLOYEE", "ADMIN"));

        responseDto =
                new EmployeeResponseDto(
                        1L,
                        "John",
                        "Doe",
                        "john@example.com",
                        Set.of("EMPLOYEE"),
                        true);
    }

    @Test
    void createEmployee_WhenValidInput_ShouldReturnCreated() throws Exception {
        // given
        when(employeeService.createEmployee(any(EmployeeCreateDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        post("/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.message").value("Employee created successfully"));

        verify(employeeService).createEmployee(any(EmployeeCreateDto.class));
    }

    @Test
    void createEmployee_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        // given
        EmployeeCreateDto invalidCreateDto =
                new EmployeeCreateDto(
                        "John",
                        "Doe",
                        "invalid-email", // invalid email format
                        "password123",
                        Set.of("EMPLOYEE"));

        // when & then
        mockMvc.perform(
                        post("/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidCreateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(employeeService, never()).createEmployee(any(EmployeeCreateDto.class));
    }

    @Test
    void updateEmployee_WhenValidInput_ShouldReturnSuccess() throws Exception {
        // given
        when(employeeService.updateEmployee(anyLong(), any(EmployeeUpdateDto.class)))
                .thenReturn(responseDto);

        // when & then
        mockMvc.perform(
                        put("/employees/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee updated successfully"));

        verify(employeeService).updateEmployee(eq(1L), any(EmployeeUpdateDto.class));
    }

    @Test
    void updateEmployee_WhenEmployeeNotFound_ShouldReturnNotFound() throws Exception {
        // given
        doThrow(new ApiException("Employee not found", HttpStatus.NOT_FOUND))
                .when(employeeService)
                .updateEmployee(anyLong(), any(EmployeeUpdateDto.class));

        // when & then
        mockMvc.perform(
                        put("/employees/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void getEmployee_WhenExists_ShouldReturnEmployee() throws Exception {
        // given
        when(employeeService.getEmployeeById(1L)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/employees/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.message").value("Employee retrieved successfully"));

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void getEmployee_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // given
        when(employeeService.getEmployeeById(1L))
                .thenThrow(new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        // when & then
        mockMvc.perform(get("/employees/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void deleteEmployee_WhenExists_ShouldReturnSuccess() throws Exception {
        // given
        doNothing().when(employeeService).deleteEmployee(1L);

        // when & then
        mockMvc.perform(delete("/employees/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee deleted successfully"));

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_WhenNotFound_ShouldReturnNotFound() throws Exception {
        // given
        doThrow(new ApiException("Employee not found", HttpStatus.NOT_FOUND))
                .when(employeeService)
                .deleteEmployee(1L);

        // when & then
        mockMvc.perform(delete("/employees/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }
}
