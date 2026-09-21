package com.vertyll.fastprod.modules.employee.dto;

import java.util.Set;

import org.jspecify.annotations.Nullable;

public record EmployeeUpdateDto(
    String firstName,
    String lastName,
    String email,
    @Nullable String password,
    Set<String> roleNames
) {
}
