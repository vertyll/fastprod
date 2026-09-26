package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequestDto(
    @NotBlank(message = "validation.currentPassword.required") String currentPassword,
    @NotBlank(message = "validation.newEmail.required") @Email(message = "validation.email.invalid") String newEmail
) {
}
