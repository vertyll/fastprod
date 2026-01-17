package com.vertyll.fastprod.role.service.impl;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.mapper.RoleMapper;
import com.vertyll.fastprod.role.repository.RoleRepository;
import com.vertyll.fastprod.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vertyll.fastprod.common.enums.RoleType;

@Service
@RequiredArgsConstructor
class RoleServiceImpl implements RoleService {

    private static final String ROLE_ALREADY_EXISTS = "Role already exists";
    private static final String ROLE_NOT_FOUND = "Role not found";
    private static final String ROLE_WITH_THIS_NAME_ALREADY_EXISTS = "Role with this name already exists";
    private static final String DEFAULT_ROLE = "Default role: ";

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleCreateDto dto) {
        if (roleRepository.existsByName(dto.name())) {
            throw new ApiException(ROLE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        Role role = roleMapper.toEntity(dto);

        Role savedRole = roleRepository.save(role);
        return roleMapper.toResponseDto(savedRole);
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(Long id, RoleUpdateDto dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

        RoleType newName = dto.name();

        if (role.getName() != newName && roleRepository.existsByName(newName)) {
            throw new ApiException(ROLE_WITH_THIS_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        roleMapper.updateFromDto(dto, role);
        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponseDto(updatedRole);
    }

    @Override
    public Role getOrCreateDefaultRole(RoleType roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = Role.builder().name(roleName).description(DEFAULT_ROLE + roleName).build();
            return roleRepository.save(role);
        });
    }

    @Override
    public RoleResponseDto getRoleById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ApiException(ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));

        return roleMapper.toResponseDto(role);
    }
}
