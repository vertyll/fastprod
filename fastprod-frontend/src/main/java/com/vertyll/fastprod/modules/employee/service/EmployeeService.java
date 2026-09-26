package com.vertyll.fastprod.modules.employee.service;

import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.shared.config.BackendApiProperties;
import com.vertyll.fastprod.shared.dto.PageResponse;
import com.vertyll.fastprod.shared.filters.FiltersValue;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import com.vertyll.fastprod.shared.service.BaseHttpService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public class EmployeeService extends BaseHttpService {

    private static final String EMPLOYEE_ENDPOINT = "/employees";

    public EmployeeService(
        BackendApiProperties backendApi,
        ObjectMapper objectMapper,
        AuthTokenProvider authTokenProvider
    ) {
        super(backendApi.url(), objectMapper, authTokenProvider);
    }

    public EmployeeResponseDto createEmployee(EmployeeCreateDto createDto) {
        return requireNonNull(post(EMPLOYEE_ENDPOINT, createDto, EmployeeResponseDto.class));
    }

    public EmployeeResponseDto updateEmployee(Long id, EmployeeUpdateDto updateDto) {
        return requireNonNull(put(EMPLOYEE_ENDPOINT + "/" + id, updateDto, EmployeeResponseDto.class));
    }

    public EmployeeResponseDto getEmployee(Long id) {
        return requireNonNull(get(EMPLOYEE_ENDPOINT + "/" + id, EmployeeResponseDto.class));
    }

    public PageResponse<EmployeeResponseDto> getAllEmployees(
        int page,
        int size,
        String sortBy,
        String sortDirection,
        FiltersValue filters
    ) {
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

    public void deleteEmployee(Long id) {
        delete(EMPLOYEE_ENDPOINT + "/" + id, Void.class);
    }
}
