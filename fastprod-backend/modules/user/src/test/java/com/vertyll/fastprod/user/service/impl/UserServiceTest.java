package com.vertyll.fastprod.user.service.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.mapper.UserMapper;
import com.vertyll.fastprod.user.repository.UserRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressFBWarnings(
    value = {
        "URF_UNREAD_FIELD",
        "HARD_CODE_PASSWORD"
    },
    justification = "Test class: unused fields and test passwords are safe"
)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy

    @SuppressWarnings("UnusedVariable")

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserCreateDto createDto;
    private UserUpdateDto updateDto;
    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name(RoleType.USER).description("Default user role").build();

        adminRole = Role.builder().name(RoleType.ADMIN).description("Admin role").build();

        createDto = new UserCreateDto("John", "Doe", "john@example.com", "password123", Set.of("USER"));

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
            .verified(true)
            .build();
    }

    @Test
    void createUser_WhenValidData_ShouldCreateUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(any(RoleType.class))).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.createUser(createDto);

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
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> userService.createUser(createDto));

        assertEquals("Email already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenValidData_ShouldUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole(RoleType.USER)).thenReturn(userRole);
        when(roleService.getOrCreateDefaultRole(RoleType.ADMIN)).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.updateUser(1L, updateDto);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        assertEquals(updateDto.firstName(), capturedUser.getFirstName());
        assertEquals(updateDto.lastName(), capturedUser.getLastName());
        assertEquals(updateDto.email(), capturedUser.getEmail());

        verify(roleService).getOrCreateDefaultRole(RoleType.USER);
        verify(roleService).getOrCreateDefaultRole(RoleType.ADMIN);
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> userService.updateUser(1L, updateDto));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user.getFirstName(), result.firstName());
        assertEquals(user.getLastName(), result.lastName());
        assertEquals(user.getEmail(), result.email());
        assertTrue(result.isVerified());
        assertEquals(1, result.roles().size());
        assertTrue(result.roles().contains(RoleType.USER));
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserById(1L));

        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateUser_WhenAddingAdminRole_ShouldUpdateUserRoles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole(RoleType.ADMIN)).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserUpdateDto updateRequest = new UserUpdateDto("John", "Doe", "john@example.com", null, Set.of("ADMIN"));

        UserResponseDto result = userService.updateUser(1L, updateRequest);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        verify(roleService).getOrCreateDefaultRole(RoleType.ADMIN);
        assertTrue(capturedUser.getRoles().contains(adminRole));
    }

    @Test
    void createUser_WhenNoRolesProvided_ShouldCreateUserWithDefaultRole() {
        UserCreateDto createDtoWithoutRoles = new UserCreateDto("Jane", "Doe", "jane@example.com", "password123", null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(RoleType.USER)).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.createUser(createDtoWithoutRoles);

        verify(roleService).getOrCreateDefaultRole(RoleType.USER);
        assertNotNull(result);
    }

    @Test
    void createUser_WhenEmptyRolesProvided_ShouldCreateUserWithDefaultRole() {
        UserCreateDto createDtoWithEmptyRoles =
                new UserCreateDto("Jane", "Doe", "jane@example.com", "password123", Set.of());

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(RoleType.USER)).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.createUser(createDtoWithEmptyRoles);

        verify(roleService).getOrCreateDefaultRole(RoleType.USER);
        assertNotNull(result);
    }

    @Test
    void updateUser_WhenNullFieldsProvided_ShouldNotUpdateNullFields() {
        @SuppressWarnings("NullAway") UserUpdateDto partialUpdateDto =
                new UserUpdateDto("Updated Name", null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.updateUser(1L, partialUpdateDto);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        assertEquals("Updated Name", capturedUser.getFirstName());
        assertEquals("Doe", capturedUser.getLastName());
        assertEquals("john@example.com", capturedUser.getEmail());
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("john@example.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("nonexistent@example.com");

        assertFalse(result);
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertEquals(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void findByEmailWithRoles_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByEmailWithRoles("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmailWithRoles("john@example.com");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByEmailWithRoles_WhenUserDoesNotExist_ShouldReturnEmpty() {
        when(userRepository.findByEmailWithRoles("nonexistent@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmailWithRoles("nonexistent@example.com");

        assertTrue(result.isEmpty());
    }
}
