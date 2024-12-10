package com.vertyll.fastprod.role.service;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.model.Role;
import com.vertyll.fastprod.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    private RoleCreateDto createDto;
    private Role role;

    @BeforeEach
    void setUp() {
        createDto = new RoleCreateDto();
        createDto.setName("ADMIN");
        createDto.setDescription("Administrator role");

        role = Role.builder()
                .name("ADMIN")
                .description("Administrator role")
                .build();
    }

    @Test
    void createRole_WhenValidData_ShouldCreateRole() {
        // given
        when(roleRepository.existsByName(anyString())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // when
        RoleResponseDto returnedRole = roleService.createRole(createDto);

        // then
        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        assertNotNull(returnedRole);
        assertEquals(createDto.getName(), capturedRole.getName());
        assertEquals(createDto.getDescription(), capturedRole.getDescription());
        assertEquals("ADMIN", returnedRole.getName());
        assertEquals("Administrator role", returnedRole.getDescription());
    }

    @Test
    void createRole_WhenRoleExists_ShouldThrowException() {
        // given
        when(roleRepository.existsByName(anyString())).thenReturn(true);

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> roleService.createRole(createDto)
        );

        assertEquals("Role already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getOrCreateDefaultRole_WhenRoleExists_ShouldReturnExistingRole() {
        // given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        // when
        Role returnedRole = roleService.getOrCreateDefaultRole("USER");

        // then
        assertNotNull(returnedRole);
        assertEquals(role.getName(), returnedRole.getName());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getOrCreateDefaultRole_WhenRoleDoesNotExist_ShouldCreateNewRole() {
        // given
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // when
        Role returnedRole = roleService.getOrCreateDefaultRole("USER");

        // then
        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();

        assertNotNull(returnedRole);
        assertEquals("USER", capturedRole.getName());
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
        assertEquals(role.getName(), returnedRole.getName());
        assertEquals(role.getDescription(), returnedRole.getDescription());
    }

    @Test
    void getRoleById_WhenRoleDoesNotExist_ShouldThrowException() {
        // given
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> roleService.getRoleById(1L)
        );

        assertEquals("Role not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}