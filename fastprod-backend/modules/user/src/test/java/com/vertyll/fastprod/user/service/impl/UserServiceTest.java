package com.vertyll.fastprod.user.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleService roleService;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    @Captor private ArgumentCaptor<User> userCaptor;

    private UserCreateDto createDto;
    private UserUpdateDto updateDto;
    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name("USER").description("Default user role").build();

        adminRole = Role.builder().name("ADMIN").description("Admin role").build();

        createDto =
                new UserCreateDto("John", "Doe", "john@example.com", "password123", Set.of("USER"));

        updateDto =
                new UserUpdateDto(
                        "John Updated",
                        "Doe Updated",
                        "john.updated@example.com",
                        null,
                        Set.of("USER", "ADMIN"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        user =
                User.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .password("encodedPassword")
                        .roles(roles)
                        .isVerified(true)
                        .build();
    }

    @Test
    void createUser_WhenValidData_ShouldCreateUser() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserResponseDto result = userService.createUser(createDto);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        assertEquals(createDto.firstName(), capturedUser.getFirstName());
        assertEquals(createDto.lastName(), capturedUser.getLastName());
        assertEquals(createDto.email(), capturedUser.getEmail());
        assertTrue(capturedUser.isVerified());
        verify(passwordEncoder).encode(createDto.password());
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> userService.createUser(createDto));

        assertEquals("Email already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenValidData_ShouldUpdateUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("USER")).thenReturn(userRole);
        when(roleService.getOrCreateDefaultRole("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserResponseDto result = userService.updateUser(1L, updateDto);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        assertEquals(updateDto.firstName(), capturedUser.getFirstName());
        assertEquals(updateDto.lastName(), capturedUser.getLastName());
        assertEquals(updateDto.email(), capturedUser.getEmail());

        verify(roleService).getOrCreateDefaultRole("USER");
        verify(roleService).getOrCreateDefaultRole("ADMIN");
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> userService.updateUser(1L, updateDto));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserResponseDto result = userService.getUserById(1L);

        // then
        assertNotNull(result);
        assertEquals(user.getFirstName(), result.firstName());
        assertEquals(user.getLastName(), result.lastName());
        assertEquals(user.getEmail(), result.email());
        assertTrue(result.isVerified());
        assertEquals(1, result.roles().size());
        assertTrue(result.roles().contains("USER"));
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> userService.getUserById(1L));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUser_WhenAddingAdminRole_ShouldUpdateUserRoles() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserUpdateDto updateRequest =
                new UserUpdateDto("John", "Doe", "john@example.com", null, Set.of("ADMIN"));

        // when
        UserResponseDto result = userService.updateUser(1L, updateRequest);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        verify(roleService).getOrCreateDefaultRole("ADMIN");
        assertTrue(capturedUser.getRoles().contains(adminRole));
    }

    @Test
    void createUser_WhenNoRolesProvided_ShouldCreateUserWithDefaultRole() {
        // given
        UserCreateDto createDtoWithoutRoles =
                new UserCreateDto("Jane", "Doe", "jane@example.com", "password123", null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole("USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserResponseDto result = userService.createUser(createDtoWithoutRoles);

        // then
        verify(roleService).getOrCreateDefaultRole("USER");
        assertNotNull(result);
    }

    @Test
    void createUser_WhenEmptyRolesProvided_ShouldCreateUserWithDefaultRole() {
        // given
        UserCreateDto createDtoWithEmptyRoles =
                new UserCreateDto("Jane", "Doe", "jane@example.com", "password123", Set.of());

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole("USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserResponseDto result = userService.createUser(createDtoWithEmptyRoles);

        // then
        verify(roleService).getOrCreateDefaultRole("USER");
        assertNotNull(result);
    }

    @Test
    void updateUser_WhenNullFieldsProvided_ShouldNotUpdateNullFields() {
        // given
        UserUpdateDto partialUpdateDto = new UserUpdateDto("Updated Name", null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserResponseDto result = userService.updateUser(1L, partialUpdateDto);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        assertEquals("Updated Name", capturedUser.getFirstName());
        assertEquals("Doe", capturedUser.getLastName());
        assertEquals("john@example.com", capturedUser.getEmail());
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // when
        boolean result = userService.existsByEmail("john@example.com");

        // then
        assertTrue(result);
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // when
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // then
        assertFalse(result);
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        // given
        when(userRepository.save(user)).thenReturn(user);

        // when
        User result = userService.saveUser(user);

        // then
        assertEquals(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void findByEmailWithRoles_WhenUserExists_ShouldReturnUser() {
        // given
        when(userRepository.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.findByEmailWithRoles("john@example.com");

        // then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByEmailWithRoles_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // given
        when(userRepository.findByEmailWithRoles("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.findByEmailWithRoles("nonexistent@example.com");

        // then
        assertTrue(result.isEmpty());
    }
}
