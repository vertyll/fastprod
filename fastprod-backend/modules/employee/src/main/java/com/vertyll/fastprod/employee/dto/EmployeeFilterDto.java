package com.vertyll.fastprod.employee.dto;

import com.vertyll.fastprod.common.dto.BaseFilterDto;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

public record EmployeeFilterDto(
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection,
        String firstName,
        String lastName,
        String email,
        Boolean isVerified,
        String roles,
        String search
) {

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
        "id", 
        "firstName", 
        "lastName", 
        "email", 
        "createdAt", 
        "updatedAt"
    );

    public EmployeeFilterDto {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100;
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "ASC";
        
        if (sortBy == null || sortBy.isBlank() || !ALLOWED_SORT_FIELDS.contains(sortBy.toLowerCase())) {
            sortBy = "id";
        }
    }

    public Pageable toPageable() {
        BaseFilterDto baseFilter = new BaseFilterDto(page, size, sortBy, sortDirection);
        return baseFilter.toPageable();
    }

    public boolean hasFilters() {
        return firstName != null || lastName != null || email != null 
            || isVerified != null || roles != null || search != null;
    }

    public boolean hasSearchTerm() {
        return search != null && !search.isBlank();
    }
}
