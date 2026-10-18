package com.vertyll.fastprod.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.user.entity.Role;
import com.vertyll.fastprod.user.repository.RoleRepository;
import com.vertyll.fastprod.user.service.RoleService;
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
