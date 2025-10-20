package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
