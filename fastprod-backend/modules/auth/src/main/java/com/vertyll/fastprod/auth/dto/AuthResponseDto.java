package com.vertyll.fastprod.auth.dto;

public record AuthResponseDto(
        String token,
        String type
) {
}
