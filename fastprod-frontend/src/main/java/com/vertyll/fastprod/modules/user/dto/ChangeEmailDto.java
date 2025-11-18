package com.vertyll.fastprod.modules.user.dto;

public record ChangeEmailDto(
        String currentPassword,
        String newEmail
) {
}
