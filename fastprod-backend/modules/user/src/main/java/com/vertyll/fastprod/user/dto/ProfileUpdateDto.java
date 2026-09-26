package com.vertyll.fastprod.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateDto(
    @NotBlank(message = "validation.firstName.required") String firstName,
    @NotBlank(message = "validation.lastName.required") String lastName
) {
}
