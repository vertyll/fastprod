package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
    @NotBlank(message = "validation.firstName.required") String firstName,
    @NotBlank(message = "validation.lastName.required") String lastName,
    @NotBlank(message = "validation.email.required") @Email(message = "validation.email.invalid") String email,
    @NotBlank(message = "validation.password.required") String password
) {
}
