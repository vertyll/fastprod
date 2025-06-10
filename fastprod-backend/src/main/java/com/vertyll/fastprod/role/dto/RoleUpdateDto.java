package com.vertyll.fastprod.role.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateDto(
        @NotBlank(message = "Name is required")
        String name,
        String description
) {
}
