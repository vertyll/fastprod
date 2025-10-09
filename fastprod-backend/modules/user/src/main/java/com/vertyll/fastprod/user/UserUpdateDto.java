package com.vertyll.fastprod.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UserUpdateDto(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        String password,

        Set<String> roleNames
) {
}
