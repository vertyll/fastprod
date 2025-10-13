package com.vertyll.fastprod.user.dto;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.user.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        boolean isVerified
) {
    public static UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.isVerified()
        );
    }
}
