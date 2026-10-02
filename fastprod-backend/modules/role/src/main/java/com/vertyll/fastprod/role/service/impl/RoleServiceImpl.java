package com.vertyll.fastprod.role.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.repository.RoleRepository;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class RoleServiceImpl implements RoleService {

    private static final String DEFAULT_ROLE = "Default role: ";

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Role getOrCreateDefaultRole(RoleType roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = Role.builder().name(roleName).description(DEFAULT_ROLE + roleName).build();
            return roleRepository.save(role);
        });
    }
}
