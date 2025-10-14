package com.vertyll.fastprod.employee.dto;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.user.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record EmployeeResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        boolean isVerified
) {
    public static EmployeeResponseDto mapToDto(User user) {
        return new EmployeeResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.isVerified()
        );
    }
}
