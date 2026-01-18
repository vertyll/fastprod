package com.vertyll.fastprod.role.dto;

import org.jspecify.annotations.Nullable;

public record RoleResponseDto(Long id, String name, @Nullable String description) {}
