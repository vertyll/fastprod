package com.vertyll.fastprod.user.service;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.role.model.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.model.User;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserCreateDto createDto;
    private UserUpdateDto updateDto;
    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .name("USER")
                .description("Default user role")
                .build();

        adminRole = Role.builder()
                .name("ADMIN")
                .description("Admin role")
                .build();

        createDto = new UserCreateDto(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                Set.of("USER")
        );

        updateDto = new UserUpdateDto(
                "John Updated",
                "Doe Updated",
                "john.updated@example.com",
                null,
                Set.of("USER", "ADMIN")
        );

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .roles(roles)
                .enabled(true)
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
        assertTrue(capturedUser.isEnabled());
        verify(passwordEncoder).encode(createDto.password());
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.createUser(createDto)
        );

        assertEquals("Email already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenValidData_ShouldUpdateUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(userRole);
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
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.updateUser(1L, updateDto)
        );

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
        assertTrue(result.enabled());
        assertEquals(1, result.roles().size());
        assertTrue(result.roles().contains("USER"));
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception = assertThrows(
                ApiException.class,
                () -> userService.getUserById(1L)
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUser_WhenAddingAdminRole_ShouldUpdateUserRoles() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserUpdateDto updateRequest = new UserUpdateDto(
                "John",
                "Doe",
                "john@example.com",
                null,
                Set.of("ADMIN")
        );

        // when
        UserResponseDto result = userService.updateUser(1L, updateRequest);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        verify(roleService).getOrCreateDefaultRole("ADMIN");
        assertTrue(capturedUser.getRoles().contains(adminRole));
    }
}
