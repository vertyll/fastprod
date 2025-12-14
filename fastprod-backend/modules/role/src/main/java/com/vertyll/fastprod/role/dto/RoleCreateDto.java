package com.vertyll.fastprod.role.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record RoleCreateDto(
        @NotBlank(message = "Name is required")
        String name,

        @Nullable
        String description
) {
}
