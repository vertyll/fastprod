package com.vertyll.fastprod.role;

public record RoleResponseDto (
    Long id,
    String name,
    String description
) {
    public static RoleResponseDto mapToDto(Role role) {
        return new RoleResponseDto(
            role.getId(),
            role.getName(),
            role.getDescription()
        );
    }
}
