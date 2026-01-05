package com.vertyll.fastprod.user.dto;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateDto(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid")
                String email,
        @Nullable String password,
        @Nullable Set<String> roleNames) {}
