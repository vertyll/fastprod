package com.vertyll.fastprod.role;

import com.vertyll.fastprod.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.vertyll.fastprod.role.RoleResponseDto.mapToDto;

@Service
@RequiredArgsConstructor
class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleCreateDto dto) {
        if (roleRepository.existsByName(dto.name())) {
            throw new ApiException("Role already exists", HttpStatus.BAD_REQUEST);
        }

        Role role = Role.builder()
                .name(dto.name())
                .description(dto.description())
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToDto(savedRole);
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(Long id, RoleUpdateDto dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Role not found", HttpStatus.NOT_FOUND));

        if (roleRepository.existsByName(dto.name()) && !role.getName().equals(dto.name())) {
            throw new ApiException("Role with this name already exists", HttpStatus.BAD_REQUEST);
        }

        role.setName(dto.name());
        role.setDescription(dto.description());

        Role updatedRole = roleRepository.save(role);
        return mapToDto(updatedRole);
    }

    @Override
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

    @Override
    public RoleResponseDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Role not found", HttpStatus.NOT_FOUND));

        return mapToDto(role);
    }
}
