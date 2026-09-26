package com.vertyll.fastprod.user.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

public record UserUpdateDto(
    @NotBlank(message = "validation.firstName.required") String firstName,
    @NotBlank(message = "validation.lastName.required") String lastName,
    @NotBlank(message = "validation.email.required") @Email(message = "validation.email.invalid") String email,
    @Nullable String password,
    @Nullable Set<String> roleNames
) {
}
