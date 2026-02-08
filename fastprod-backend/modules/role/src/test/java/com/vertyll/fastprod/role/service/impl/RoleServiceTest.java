package com.vertyll.fastprod.role.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.mapper.RoleMapper;
import com.vertyll.fastprod.role.repository.RoleRepository;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(
        value = {"URF_UNREAD_FIELD"},
        justification = "Test class: roleMapper is used by Mockito injection")
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;

    @Spy
    @SuppressWarnings("UnusedVariable")
    private final RoleMapper roleMapper = Mappers.getMapper(RoleMapper.class);

    @InjectMocks private RoleServiceImpl roleService;

    @Captor private ArgumentCaptor<Role> roleCaptor;

    private RoleCreateDto createDto;
    private Role role;

    @BeforeEach
    void setUp() {
        createDto = new RoleCreateDto("ADMIN", "Administrator role");

        role = Role.builder().name(RoleType.ADMIN).description("Administrator role").build();
    }

    @Test
    void createRole_WhenValidData_ShouldCreateRole() {
        // given
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // when
        RoleResponseDto returnedRole = roleService.createRole(createDto);

        // then
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
        // given
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(true);

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> roleService.createRole(createDto));

        assertEquals("Role already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_WhenValidData_ShouldUpdateRole() {
        // given
        Role existingRole =
                Role.builder().name(RoleType.ADMIN).description("Old description").build();
        existingRole.setId(1L);

        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Updated description");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(existingRole);

        // when
        RoleResponseDto result = roleService.updateRole(1L, updateDto);

        // then
        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        // Assertions for the captured entity (Database state)
        assertEquals(RoleType.ADMIN, capturedRole.getName());
        assertEquals("Updated description", capturedRole.getDescription());

        // Assertions for the 'result' variable (API Response)
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ADMIN", result.name());
        assertEquals("Updated description", result.description());
    }

    @Test
    void updateRole_WhenRoleNotFound_ShouldThrowException() {
        // given
        RoleUpdateDto updateDto = new RoleUpdateDto("ADMIN", "Updated description");

        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> roleService.updateRole(1L, updateDto));

        assertEquals("Role not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_WhenNameAlreadyExists_ShouldThrowException() {
        // given
        Role existingRole =
                Role.builder().name(RoleType.ADMIN).description("Old description").build();
        existingRole.setId(1L); // Set ID after building

        RoleUpdateDto updateDto =
                new RoleUpdateDto(
                        "USER", "Updated description"); // Different name than existing role

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByName(RoleType.USER))
                .thenReturn(true); // Name conflict with another role

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> roleService.updateRole(1L, updateDto));

        assertEquals("Role with this name already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getOrCreateDefaultRole_WhenRoleExists_ShouldReturnExistingRole() {
        // given
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(role));

        // when
        Role returnedRole = roleService.getOrCreateDefaultRole(RoleType.USER);

        // then
        assertNotNull(returnedRole);
        assertEquals(role.getName(), returnedRole.getName());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getOrCreateDefaultRole_WhenRoleDoesNotExist_ShouldCreateNewRole() {
        // given
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // when
        Role returnedRole = roleService.getOrCreateDefaultRole(RoleType.USER);

        // then
        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        assertNotNull(returnedRole);
        assertEquals("USER", capturedRole.getName().name());
        assertTrue(capturedRole.getDescription().contains("USER"));
    }

    @Test
    void getRoleById_WhenRoleExists_ShouldReturnRole() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        // when
        RoleResponseDto returnedRole = roleService.getRoleById(1L);

        // then
        assertNotNull(returnedRole);
        assertEquals(role.getName().name(), returnedRole.name());
        assertEquals(role.getDescription(), returnedRole.description());
    }

    @Test
    void getRoleById_WhenRoleDoesNotExist_ShouldThrowException() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> roleService.getRoleById(1L));

        assertEquals("Role not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
