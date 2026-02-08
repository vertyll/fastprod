package com.vertyll.fastprod.user.dto;

import java.util.Set;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<RoleType> roles,
        boolean isVerified) {}
