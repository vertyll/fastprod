package com.vertyll.fastprod.modules.user.dto;

public record UserProfileDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        java.util.Set<String> roles,
        boolean isVerified) {}
