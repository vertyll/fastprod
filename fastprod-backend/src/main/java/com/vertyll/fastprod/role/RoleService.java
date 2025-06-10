package com.vertyll.fastprod.role;

public interface RoleService {
    RoleResponseDto createRole(RoleCreateDto dto);
    RoleResponseDto updateRole(Long id, RoleUpdateDto dto);
    Role getOrCreateDefaultRole(String roleName);
    RoleResponseDto getRoleById(Long id);
}
