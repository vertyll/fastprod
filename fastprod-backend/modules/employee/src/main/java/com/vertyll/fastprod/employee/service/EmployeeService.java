package com.vertyll.fastprod.employee.service;

import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    @Transactional
    EmployeeResponseDto createEmployee(EmployeeCreateDto dto);

    @Transactional
    EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto dto);

    EmployeeResponseDto getEmployeeById(Long id);

    Page<EmployeeResponseDto> getAllEmployees(Pageable pageable);

    @Transactional
    void deleteEmployee(Long id);
}
