package com.vertyll.fastprod.auth.dto;

public record AuthResponseDto(
        String token,
        String type
) {
    public static AuthResponseDto mapToDto(
            String token,
            String type
    ) {
        return new AuthResponseDto(
                token,
                type
        );
    }
}
