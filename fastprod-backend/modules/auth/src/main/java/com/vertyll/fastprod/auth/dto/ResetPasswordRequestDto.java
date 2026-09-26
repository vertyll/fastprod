package com.vertyll.fastprod.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(@NotBlank(message = "validation.newPassword.required") String newPassword) {
}
