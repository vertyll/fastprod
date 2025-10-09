package com.vertyll.fastprod.user;

import com.vertyll.fastprod.role.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        boolean enabled
) {
    public static UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.isEnabled()
        );
    }
}
