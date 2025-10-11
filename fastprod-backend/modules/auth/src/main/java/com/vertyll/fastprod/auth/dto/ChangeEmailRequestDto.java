package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequestDto(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New email is required")
        @Email(message = "Email should be valid")
        String newEmail
) {
}
