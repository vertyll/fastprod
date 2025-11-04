package com.vertyll.fastprod.modules.employee.dto;

import java.util.Set;

public record EmployeeResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        boolean isVerified
) {
}
