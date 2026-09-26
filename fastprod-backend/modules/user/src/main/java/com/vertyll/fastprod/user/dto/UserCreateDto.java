package com.vertyll.fastprod.user.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

public record UserCreateDto(
    @NotBlank(message = "validation.firstName.required") String firstName,
    @NotBlank(message = "validation.lastName.required") String lastName,
    @NotBlank(message = "validation.email.required") @Email(message = "validation.email.invalid") String email,
    @NotBlank(message = "validation.password.required") String password,
    @Nullable Set<String> roleNames
) {
}
