package com.vertyll.fastprod.employee.controller;

import java.util.Set;

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

import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.service.EmployeeService;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @SuppressWarnings("NullAway")
    @Mock
    private EmployeeService employeeService;

    @SuppressWarnings("NullAway")
    @InjectMocks
    private EmployeeController employeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private EmployeeCreateDto createDto;
    private EmployeeUpdateDto updateDto;
    private EmployeeResponseDto responseDto;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();

        createDto = new EmployeeCreateDto("John", "Doe", "john@example.com", "password123", Set.of("EMPLOYEE"));

        updateDto = new EmployeeUpdateDto(
            "John Updated",
            "Doe Updated",
            "john.updated@example.com",
            null,
            Set.of("EMPLOYEE", "ADMIN")
        );

        responseDto = new EmployeeResponseDto(1L, "John", "Doe", "john@example.com", Set.of(RoleType.EMPLOYEE), true);
    }

    @AfterEach
    void tearDown() {
        if (validator != null) {
            validator.close();
        }
    }

    @Test
    void createEmployee_WhenValidInput_ShouldReturnCreated() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateDto.class))).thenReturn(responseDto);

        mockMvc
            .perform(
                post("/employees").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto))
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"));

        verify(employeeService).createEmployee(any(EmployeeCreateDto.class));
    }

    @Test
    void createEmployee_WhenInvalidInput_ShouldReturnBadRequest() throws Exception {
        EmployeeCreateDto invalidCreateDto =
                new EmployeeCreateDto("John", "Doe", "invalid-email", "password123", Set.of("EMPLOYEE"));

        mockMvc
            .perform(
                post("/employees").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidCreateDto))
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("errors.common.validationFailed"));

        verify(employeeService, never()).createEmployee(any(EmployeeCreateDto.class));
    }

    @Test
    void updateEmployee_WhenValidInput_ShouldReturnSuccess() throws Exception {
        when(employeeService.updateEmployee(anyLong(), any(EmployeeUpdateDto.class))).thenReturn(responseDto);

        mockMvc
            .perform(
                put("/employees/1").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
            .andDo(print())
            .andExpect(status().isOk());

        verify(employeeService).updateEmployee(eq(1L), any(EmployeeUpdateDto.class));
    }

    @Test
    void updateEmployee_WhenEmployeeNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new ApiException("errors.employee.notFound", HttpStatus.NOT_FOUND)).when(employeeService)
            .updateEmployee(anyLong(), any(EmployeeUpdateDto.class));

        mockMvc
            .perform(
                put("/employees/1").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("errors.employee.notFound"));
    }

    @Test
    void getEmployee_WhenExists_ShouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/employees/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"));

        verify(employeeService).getEmployeeById(1L);
    }

    @Test
    void getEmployee_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeById(1L))
            .thenThrow(new ApiException("errors.employee.notFound", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/employees/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("errors.employee.notFound"));
    }

    @Test
    void deleteEmployee_WhenExists_ShouldReturnSuccess() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/employees/1")).andDo(print()).andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void deleteEmployee_WhenNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new ApiException("errors.employee.notFound", HttpStatus.NOT_FOUND)).when(employeeService)
            .deleteEmployee(1L);

        mockMvc.perform(delete("/employees/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("errors.employee.notFound"));
    }
}
