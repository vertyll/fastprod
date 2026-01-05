package com.vertyll.fastprod.role.service;

import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.entity.Role;

public interface RoleService {
    @Transactional
    RoleResponseDto createRole(RoleCreateDto dto);

    @Transactional
    RoleResponseDto updateRole(Long id, RoleUpdateDto dto);

    Role getOrCreateDefaultRole(String roleName);

    RoleResponseDto getRoleById(Long id);
}
