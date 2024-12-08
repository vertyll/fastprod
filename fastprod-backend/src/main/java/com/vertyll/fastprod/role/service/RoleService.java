package com.vertyll.fastprod.role.service;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.model.Role;
import com.vertyll.fastprod.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public RoleResponseDto createRole(RoleCreateDto dto) {
        if (roleRepository.existsByName(dto.getName())) {
            throw new ApiException("Role already exists", HttpStatus.BAD_REQUEST);
        }

        Role role = Role.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToDto(savedRole);
    }

    public Role getOrCreateDefaultRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .description("Default role: " + roleName)
                            .build();
                    return roleRepository.save(role);
                });
    }

    public RoleResponseDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Role not found", HttpStatus.NOT_FOUND));
        return mapToDto(role);
    }

    private RoleResponseDto mapToDto(Role role) {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}