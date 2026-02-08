package com.vertyll.fastprod.sharedinfrastructure.dto;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.google.common.base.Ascii;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BaseFilterDto(
        @Nullable @Min(value = 0, message = "Page number must be greater than or equal to 0")
                Integer page,
        @Nullable
                @Min(value = 1, message = "Page size must be at least 1")
                @Max(value = 100, message = "Page size must not exceed 100")
                Integer size,
        @Nullable String sortBy,
        @Nullable String sortDirection) {
    public BaseFilterDto {
        page = (page != null && page >= 0) ? page : 0;
        size = (size != null && size >= 1 && size <= 100) ? size : 10;

        if (sortBy != null && !sortBy.isBlank()) {
            sortBy = sortBy.trim();
        } else {
            sortBy = "id";
        }

        if (sortDirection != null && !sortDirection.isBlank()) {
            sortDirection = Ascii.toUpperCase(sortDirection.trim());
        } else {
            sortDirection = "ASC";
        }
    }

    public Pageable toPageable() {
        int pageValue = Objects.requireNonNull(page, "page cannot be null after normalization");
        int sizeValue = Objects.requireNonNull(size, "size cannot be null after normalization");
        String sortByValue =
                Objects.requireNonNull(sortBy, "sortBy cannot be null after normalization");
        String sortDirectionValue =
                Objects.requireNonNull(
                        sortDirection, "sortDirection cannot be null after normalization");

        Sort.Direction direction =
                "DESC".equals(sortDirectionValue) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(pageValue, sizeValue, Sort.by(direction, sortByValue));
    }
}
