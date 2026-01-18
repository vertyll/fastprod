package com.vertyll.fastprod.role.dto;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotBlank;

public record RoleUpdateDto(
        @NotBlank(message = "Name is required") String name, @Nullable String description) {}
