package com.vertyll.fastprod.employee.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeFilterDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.service.EmployeeService;
import com.vertyll.fastprod.sharedinfrastructure.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management APIs")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new employee")
    public ResponseEntity<EmployeeResponseDto> createEmployee(@RequestBody @Valid EmployeeCreateDto dto) {
        EmployeeResponseDto employee = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing employee")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
        @PathVariable Long id,
        @RequestBody @Valid EmployeeUpdateDto dto
    ) {
        EmployeeResponseDto employee = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeResponseDto> getEmployee(@PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get all employees with pagination and filters")
    public ResponseEntity<PageResponse<EmployeeResponseDto>> getAllEmployees(
        @Valid @ModelAttribute EmployeeFilterDto filterDto
    ) {
        Page<EmployeeResponseDto> employees = employeeService.getAllEmployees(filterDto);
        return ResponseEntity.ok(PageResponse.from(employees));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
