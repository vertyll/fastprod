package com.vertyll.fastprod.user.dto;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        boolean isVerified) {}
