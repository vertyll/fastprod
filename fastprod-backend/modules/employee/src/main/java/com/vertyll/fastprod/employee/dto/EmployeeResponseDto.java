package com.vertyll.fastprod.employee.dto;

import java.util.Set;

import com.vertyll.fastprod.common.enums.RoleType;

public record EmployeeResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<RoleType> roles,
        boolean isVerified) {}
