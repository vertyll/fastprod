package com.vertyll.fastprod.modules.user.dto;

public record ChangePasswordDto(
        String currentPassword,
        String newPassword
) {
}
