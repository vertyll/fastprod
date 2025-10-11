package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
        @NotBlank(message = "New password is required")
        String newPassword
) {
}