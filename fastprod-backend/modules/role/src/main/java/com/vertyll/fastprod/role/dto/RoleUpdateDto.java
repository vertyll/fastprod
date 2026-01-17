package com.vertyll.fastprod.role.dto;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.common.enums.RoleType;

import jakarta.validation.constraints.NotNull;

public record RoleUpdateDto(
        @NotNull(message = "Name is required") RoleType name, @Nullable String description) {}
