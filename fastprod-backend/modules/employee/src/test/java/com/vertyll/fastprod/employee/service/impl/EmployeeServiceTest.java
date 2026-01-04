package com.vertyll.fastprod.employee.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.vertyll.fastprod.common.exception.ApiException;
import com.vertyll.fastprod.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.employee.mapper.EmployeeMapper;
import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.repository.UserRepository;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @SuppressWarnings("UnusedVariable")
    @Spy
    private EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private EmployeeCreateDto createDto;
    private EmployeeUpdateDto updateDto;
    private User user;
    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        employeeRole = Role.builder()
                .name("EMPLOYEE")
                .description("Employee role")
                .build();

        adminRole = Role.builder()
                .name("ADMIN")
                .description("Admin role")
                .build();

        createDto =
                new EmployeeCreateDto(
                        "John",
                        "Doe",
                        "john@example.com",
                        "password123",
                        Set.of("EMPLOYEE"));

        updateDto =
                new EmployeeUpdateDto(
                        "John Updated",
                        "Doe Updated",
                        "john.updated@example.com",
                        null,
                        Set.of("EMPLOYEE", "ADMIN"));

        Set<Role> roles = new HashSet<>();
        roles.add(employeeRole);

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .roles(roles)
                .verified(true)
                .active(true)
                .build();
    }

    @Test
    void createEmployee_WhenValidData_ShouldCreateEmployee() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole(anyString())).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.createEmployee(createDto);

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
    void createEmployee_WhenEmailExists_ShouldThrowException() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> employeeService.createEmployee(createDto));

        assertEquals("Email already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateEmployee_WhenValidData_ShouldUpdateEmployee() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("EMPLOYEE")).thenReturn(employeeRole);
        when(roleService.getOrCreateDefaultRole("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateDto);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        assertEquals(updateDto.firstName(), capturedUser.getFirstName());
        assertEquals(updateDto.lastName(), capturedUser.getLastName());
        assertEquals(updateDto.email(), capturedUser.getEmail());

        verify(roleService).getOrCreateDefaultRole("EMPLOYEE");
        verify(roleService).getOrCreateDefaultRole("ADMIN");
    }

    @Test
    void updateEmployee_WhenEmployeeNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(
                        ApiException.class,
                        () -> employeeService.updateEmployee(1L, updateDto));

        assertEquals("Employee not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateEmployee_WhenEmployeeInactive_ShouldThrowException() {
        // given
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        ApiException exception =
                assertThrows(
                        ApiException.class,
                        () -> employeeService.updateEmployee(1L, updateDto));

        assertEquals("Cannot update inactive employee", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void updateEmployee_WhenEmailAlreadyExists_ShouldThrowException() {
        // given
        EmployeeUpdateDto dtoWithDifferentEmail =
                new EmployeeUpdateDto(
                        "John",
                        "Doe",
                        "different@example.com",
                        null,
                        Set.of("EMPLOYEE"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("different@example.com")).thenReturn(true);

        // when & then
        ApiException exception =
                assertThrows(
                        ApiException.class,
                        () -> employeeService.updateEmployee(1L, dtoWithDifferentEmail));

        assertEquals("Email already exists", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getEmployeeById_WhenEmployeeExists_ShouldReturnEmployee() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        EmployeeResponseDto result = employeeService.getEmployeeById(1L);

        // then
        assertNotNull(result);
        assertEquals(user.getFirstName(), result.firstName());
        assertEquals(user.getLastName(), result.lastName());
        assertEquals(user.getEmail(), result.email());
        assertTrue(result.isVerified());
        assertEquals(1, result.roles().size());
        assertTrue(result.roles().contains("EMPLOYEE"));
    }

    @Test
    void getEmployeeById_WhenEmployeeNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> employeeService.getEmployeeById(1L));

        assertEquals("Employee not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getEmployeeById_WhenEmployeeInactive_ShouldThrowException() {
        // given
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> employeeService.getEmployeeById(1L));

        assertEquals("Employee not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void deleteEmployee_WhenEmployeeExists_ShouldSoftDeleteEmployee() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        employeeService.deleteEmployee(1L);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertFalse(capturedUser.isActive());
    }

    @Test
    void deleteEmployee_WhenEmployeeNotFound_ShouldThrowException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> employeeService.deleteEmployee(1L));

        assertEquals("Employee not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void deleteEmployee_WhenEmployeeAlreadyDeleted_ShouldThrowException() {
        // given
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when & then
        ApiException exception =
                assertThrows(ApiException.class, () -> employeeService.deleteEmployee(1L));

        assertEquals("Employee already deleted", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void updateEmployee_WhenAddingAdminRole_ShouldUpdateEmployeeRoles() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        EmployeeUpdateDto updateRequest =
                new EmployeeUpdateDto(
                        "John",
                        "Doe",
                        "john@example.com",
                        null,
                        Set.of("ADMIN"));

        // when
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateRequest);

        // then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNotNull(result);
        verify(roleService).getOrCreateDefaultRole("ADMIN");
        assertTrue(capturedUser.getRoles().contains(adminRole));
    }

    @Test
    void createEmployee_WhenNoRolesProvided_ShouldCreateEmployeeWithDefaultRole() {
        // given
        EmployeeCreateDto createDtoWithoutRoles =
                new EmployeeCreateDto(
                        "Jane",
                        "Doe",
                        "jane@example.com",
                        "password123",
                        null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole("EMPLOYEE")).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.createEmployee(createDtoWithoutRoles);

        // then
        verify(roleService).getOrCreateDefaultRole("EMPLOYEE");
        assertNotNull(result);
    }

    @Test
    void createEmployee_WhenEmptyRolesProvided_ShouldCreateEmployeeWithDefaultRole() {
        // given
        EmployeeCreateDto createDtoWithEmptyRoles =
                new EmployeeCreateDto(
                        "Jane",
                        "Doe",
                        "jane@example.com",
                        "password123",
                        Set.of());

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleService.getOrCreateDefaultRole("EMPLOYEE")).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.createEmployee(createDtoWithEmptyRoles);

        // then
        verify(roleService).getOrCreateDefaultRole("EMPLOYEE");
        assertNotNull(result);
    }

    @Test
    void updateEmployee_WhenPasswordProvided_ShouldEncodePassword() {
        // given
        EmployeeUpdateDto updateWithPassword =
                new EmployeeUpdateDto(
                        "John",
                        "Doe",
                        "john@example.com",
                        "newPassword123",
                        Set.of("EMPLOYEE"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(roleService.getOrCreateDefaultRole("EMPLOYEE")).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateWithPassword);

        // then
        verify(userRepository).save(userCaptor.capture());

        assertNotNull(result);
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void updateEmployee_WhenPasswordNull_ShouldNotEncodePassword() {
        // given
        EmployeeUpdateDto updateWithoutPassword =
                new EmployeeUpdateDto(
                        "John",
                        "Doe",
                        "john@example.com",
                        null,
                        Set.of("EMPLOYEE"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("EMPLOYEE")).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateWithoutPassword);

        // then
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateEmployee_WhenPasswordBlank_ShouldNotEncodePassword() {
        // given
        EmployeeUpdateDto updateWithBlankPassword =
                new EmployeeUpdateDto(
                        "John",
                        "Doe",
                        "john@example.com",
                        "   ",
                        Set.of("EMPLOYEE"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole("EMPLOYEE")).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        EmployeeResponseDto result = employeeService.updateEmployee(1L, updateWithBlankPassword);

        // then
        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
    }
}
