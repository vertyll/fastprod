package com.vertyll.fastprod.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto (
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    @NotBlank(message = "Password is required")
    String password
) {
}