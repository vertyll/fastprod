package com.vertyll.fastprod.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record BaseFilterDto(
        @Min(value = 0, message = "Page number must be greater than or equal to 0")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size must not exceed 100")
        Integer size,

        String sortBy,

        String sortDirection
) {
    public BaseFilterDto {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "ASC";
    }

    public Pageable toPageable() {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
