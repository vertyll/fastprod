package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequestDto(
    @NotBlank(message = "validation.email.required") @Email(message = "validation.email.invalid") String email
) {
}
