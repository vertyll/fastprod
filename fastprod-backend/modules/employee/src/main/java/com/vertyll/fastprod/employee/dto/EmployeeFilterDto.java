package com.vertyll.fastprod.employee.dto;

import com.vertyll.fastprod.common.dto.BaseFilterDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public record EmployeeFilterDto(
        @Nullable @Min(0) Integer page,
        @Nullable @Min(1) @Max(100) Integer size,
        @Nullable String sortBy,
        @Nullable String sortDirection,

        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String email,
        @Nullable Boolean isVerified,
        @Nullable String roles,
        @Nullable String search
) {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "firstName", "lastName", "email", "createdAt", "updatedAt"
    );

    public EmployeeFilterDto {
        if (sortBy != null && !sortBy.isBlank() && ALLOWED_SORT_FIELDS.contains(sortBy.trim())) {
            sortBy = sortBy.trim();
        } else {
            sortBy = "id";
        }

        firstName = trimOrNull(firstName);
        lastName = trimOrNull(lastName);
        email = trimOrNull(email);
        roles = trimOrNull(roles);
        search = trimOrNull(search);
    }

    private static String trimOrNull(@Nullable String value) {
        return (value != null && !value.isBlank()) ? value.trim() : null;
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
        return search != null;
    }
}
