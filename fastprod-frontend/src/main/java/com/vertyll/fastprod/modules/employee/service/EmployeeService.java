package com.vertyll.fastprod.modules.employee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.modules.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.dto.PageResponse;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;
import com.vertyll.fastprod.shared.filters.FiltersValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeService extends BaseHttpService {

    private static final String EMPLOYEE_ENDPOINT = "/employees";

    public EmployeeService(
        @Value("${api.backend.url}") String backendUrl, 
        ObjectMapper objectMapper,
        AuthTokenProvider authTokenProvider
    ) {
        super(backendUrl, objectMapper, authTokenProvider);
    }

    public ApiResponse<EmployeeResponseDto> createEmployee(EmployeeCreateDto createDto) throws Exception {
        return post(EMPLOYEE_ENDPOINT, createDto, EmployeeResponseDto.class);
    }

    public ApiResponse<EmployeeResponseDto> updateEmployee(Long id, EmployeeUpdateDto updateDto) throws Exception {
        return put(EMPLOYEE_ENDPOINT + "/" + id, updateDto, EmployeeResponseDto.class);
    }

    public ApiResponse<EmployeeResponseDto> getEmployee(Long id) throws Exception {
        return get(EMPLOYEE_ENDPOINT + "/" + id, EmployeeResponseDto.class);
    }

    public PageResponse<EmployeeResponseDto> getAllEmployees(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            FiltersValue filters
    ) throws Exception {
        String base = String.format(
                "%s?page=%d&size=%d&sortBy=%s&sortDirection=%s",
                EMPLOYEE_ENDPOINT,
                page,
                size,
                sortBy,
                sortDirection
        );
        String queryFilters = filters != null ? filters.toQueryString() : "";
        String endpoint = queryFilters == null || queryFilters.isBlank() ? base : (base + "&" + queryFilters);
        return getPaginated(endpoint, EmployeeResponseDto.class);
    }

    public ApiResponse<Void> deleteEmployee(Long id) throws Exception {
        return delete(EMPLOYEE_ENDPOINT + "/" + id, Void.class);
    }
}
