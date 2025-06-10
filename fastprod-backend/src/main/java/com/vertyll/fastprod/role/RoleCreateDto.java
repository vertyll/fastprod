package com.vertyll.fastprod.role;

import jakarta.validation.constraints.NotBlank;

public record RoleCreateDto (
        @NotBlank(message = "Name is required")
        String name,
        String description
) {
}
