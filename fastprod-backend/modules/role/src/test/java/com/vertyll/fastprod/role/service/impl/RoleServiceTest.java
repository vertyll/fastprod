package com.vertyll.fastprod.role.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.repository.RoleRepository;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder().name(RoleType.USER).description("User role").build();
    }

    @Test
    void getOrCreateDefaultRole_WhenRoleExists_ShouldReturnExistingRole() {
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(role));

        Role returnedRole = roleService.getOrCreateDefaultRole(RoleType.USER);

        assertNotNull(returnedRole);
        assertEquals(role.getName(), returnedRole.getName());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getOrCreateDefaultRole_WhenRoleDoesNotExist_ShouldCreateNewRole() {
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role returnedRole = roleService.getOrCreateDefaultRole(RoleType.USER);

        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        assertNotNull(returnedRole);
        assertEquals("USER", capturedRole.getName().name());
        assertTrue(capturedRole.getDescription().contains("USER"));
    }
}
