package com.vertyll.fastprod.role.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.mapper.RoleMapper;
import com.vertyll.fastprod.role.repository.RoleRepository;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressFBWarnings(
    value = {
        "URF_UNREAD_FIELD"
    },
    justification = "Test class: roleMapper is used by Mockito injection"
)
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;

    @Spy

    @SuppressWarnings("UnusedVariable")

    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);

    @InjectMocks
    private RoleServiceImpl roleService;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    private RoleCreateDto createDto;
    private Role role;

    @BeforeEach
    void setUp() {
        createDto = new RoleCreateDto("ADMIN", "Administrator role");

        role = Role.builder().name(RoleType.ADMIN).description("Administrator role").build();
    }

    @Test
    void createRole_WhenValidData_ShouldCreateRole() {
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleResponseDto returnedRole = roleService.createRole(createDto);

        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        assertNotNull(returnedRole);
        assertEquals(createDto.name(), capturedRole.getName().name());
        assertEquals(createDto.description(), capturedRole.getDescription());
        assertEquals("ADMIN", returnedRole.name());
        assertEquals("Administrator role", returnedRole.description());
    }

    @Test
    void createRole_WhenRoleExists_ShouldThrowException() {
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> roleService.createRole(createDto));

        assertEquals("Role already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_WhenValidData_ShouldUpdateRole() {
        Role existingRole = Role.builder().name(RoleType.ADMIN).description("Old description").build();
        ReflectionTestUtils.setField(existingRole, "id", 1L);

        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Updated description");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        RoleResponseDto result = roleService.updateRole(1L, updateDto);

        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        assertEquals(RoleType.ADMIN, capturedRole.getName());
        assertEquals("Updated description", capturedRole.getDescription());

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ADMIN", result.name());
        assertEquals("Updated description", result.description());
    }

    @Test
    void updateRole_WhenRoleNotFound_ShouldThrowException() {
        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Updated description");

        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> roleService.updateRole(1L, updateDto));

        assertEquals("Role not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_WhenNameAlreadyExists_ShouldThrowException() {
        Role existingRole = Role.builder().name(RoleType.ADMIN).description("Old description").build();
        ReflectionTestUtils.setField(existingRole, "id", 1L);

        RoleUpdateDto updateDto = new RoleUpdateDto("USER", "Updated description");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByName(RoleType.USER)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> roleService.updateRole(1L, updateDto));

        assertEquals("Role with this name already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
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

    @Test
    void getRoleById_WhenRoleExists_ShouldReturnRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleResponseDto returnedRole = roleService.getRoleById(1L);

        assertNotNull(returnedRole);
        assertEquals(role.getName().name(), returnedRole.name());
        assertEquals(role.getDescription(), returnedRole.description());
    }

    @Test
    void getRoleById_WhenRoleDoesNotExist_ShouldThrowException() {
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> roleService.getRoleById(1L));

        assertEquals("Role not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
