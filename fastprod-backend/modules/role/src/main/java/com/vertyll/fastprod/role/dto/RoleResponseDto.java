package com.vertyll.fastprod.role.dto;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.common.enums.RoleType;

public record RoleResponseDto(Long id, RoleType name, @Nullable String description) {}
