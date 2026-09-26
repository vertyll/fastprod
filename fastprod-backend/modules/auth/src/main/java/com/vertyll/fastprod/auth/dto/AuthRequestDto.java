package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

public record AuthRequestDto(
    @NotBlank(message = "validation.email.required") @Email(message = "validation.email.invalid") String email,
    @NotBlank(message = "validation.password.required") String password,
    @Nullable String deviceInfo
) {
}
