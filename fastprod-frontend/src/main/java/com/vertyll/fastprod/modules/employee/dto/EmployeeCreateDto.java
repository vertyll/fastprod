package com.vertyll.fastprod.modules.employee.dto;

import java.util.Set;

public record EmployeeCreateDto(
        String firstName, String lastName, String email, String password, Set<String> roleNames) {}
