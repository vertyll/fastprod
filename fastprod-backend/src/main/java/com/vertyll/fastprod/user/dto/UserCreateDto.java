package com.vertyll.fastprod.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UserCreateDto(
    @NotBlank(message = "First name is required")
    String firstName,
    @NotBlank(message = "Last name is required")
    String lastName,
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    @NotBlank(message = "Password is required")
    String password,
    Set<String> roleNames
) {
}