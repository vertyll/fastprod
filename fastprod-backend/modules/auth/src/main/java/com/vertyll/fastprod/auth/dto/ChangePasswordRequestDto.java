package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequestDto(
    @NotBlank(message = "validation.currentPassword.required") String currentPassword,
    @NotBlank(message = "validation.newPassword.required") String newPassword
) {
}
