package com.vertyll.fastprod.employee.controller;

import com.vertyll.fastprod.common.response.ApiResponse;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management APIs")
class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new employee")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> createEmployee(
            @RequestBody @Valid EmployeeCreateDto dto
    ) {
        EmployeeResponseDto employee = employeeService.createEmployee(dto);
        return ApiResponse.buildResponse(employee, "Employee created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing employee")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUpdateDto dto
    ) {
        EmployeeResponseDto employee = employeeService.updateEmployee(id, dto);
        return ApiResponse.buildResponse(employee, "Employee updated successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> getEmployee(
            @PathVariable Long id
    ) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ApiResponse.buildResponse(employee, "Employee retrieved successfully", HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get all employees")
    public ResponseEntity<ApiResponse<java.util.List<EmployeeResponseDto>>> getAllEmployees() {
        java.util.List<EmployeeResponseDto> employees = employeeService.getAllEmployees();
        return ApiResponse.buildResponse(employees, "Employees retrieved successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ApiResponse.buildResponse(null, "Employee deleted successfully", HttpStatus.OK);
    }
}
