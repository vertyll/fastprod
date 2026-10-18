package com.vertyll.fastprod.user.service.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.user.api.UserAccounts;
import com.vertyll.fastprod.user.entity.Role;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.identity.IdentityProvider;
import com.vertyll.fastprod.user.repository.UserRepository;
import com.vertyll.fastprod.user.service.RoleService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountsImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private IdentityProvider identityProvider;

    @InjectMocks
    private UserAccountsImpl accounts;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;
    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        employeeRole = Role.builder().name(RoleType.EMPLOYEE).description("Employee role").build();
        adminRole = Role.builder().name(RoleType.ADMIN).description("Admin role").build();
        Set<Role> roles = new HashSet<>();
        roles.add(employeeRole);
        user = User.builder()
            .firstName("John")
            .lastName("Doe")
            .keycloakId("kc-john")
            .email("john@example.com")
            .roles(roles)
            .verified(true)
            .active(true)
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void create_ShouldCreateTheKeycloakAccountAndTheRecord() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(identityProvider.createUser(any(), anyString(), any())).thenReturn("kc-new");
        when(roleService.getOrCreateDefaultRole(RoleType.EMPLOYEE)).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        accounts.create(
            new UserAccounts.NewAccount("John", "Doe", "john@example.com", "password123", Set.of(RoleType.EMPLOYEE))
        );

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals("john@example.com", saved.getEmail());
        assertEquals("kc-new", saved.getKeycloakId());
        assertFalse(saved.isVerified());
        assertEquals(Set.of(employeeRole), saved.getRoles());
        verify(identityProvider).createUser(
            new IdentityProvider.NewIdentity("john@example.com", "John", "Doe"),
            "password123",
            Set.of(RoleType.EMPLOYEE)
        );
    }

    @Test
    void create_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        UserAccounts.NewAccount account = new UserAccounts.NewAccount(
            "John",
            "Doe",
            "john@example.com",
            "password123",
            Set.of(RoleType.EMPLOYEE)
        );

        ApiException exception = assertThrows(ApiException.class, () -> accounts.create(account));

        assertEquals("errors.user.emailTaken", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void find_ShouldShowTheAccountWithItsRoles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserAccounts.Account account = accounts.find(1L).orElseThrow();

        assertEquals("john@example.com", account.email());
        assertEquals(Set.of(RoleType.EMPLOYEE), account.roles());
        assertTrue(account.verified());
        assertTrue(account.active());
    }

    @Test
    void update_ShouldChangeTheDetailsAndRolesHereAndInKeycloak() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleService.getOrCreateDefaultRole(RoleType.EMPLOYEE)).thenReturn(employeeRole);
        when(roleService.getOrCreateDefaultRole(RoleType.ADMIN)).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        accounts.update(
            1L,
            new UserAccounts.AccountChanges(
                "John Updated",
                "Doe Updated",
                "john.updated@example.com",
                null,
                Set.of(RoleType.EMPLOYEE, RoleType.ADMIN)
            )
        );

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("John Updated", saved.getFirstName());
        assertEquals("Doe Updated", saved.getLastName());
        assertEquals("john.updated@example.com", saved.getEmail());
        assertEquals(Set.of(employeeRole, adminRole), saved.getRoles());
        verify(identityProvider).updateUser(
            "kc-john",
            new IdentityProvider.NewIdentity("john.updated@example.com", "John Updated", "Doe Updated")
        );
        verify(identityProvider).replaceRoles("kc-john", Set.of(RoleType.EMPLOYEE, RoleType.ADMIN));
        verify(identityProvider, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void update_WhenNoRolesGiven_ShouldLeaveThem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        accounts.update(1L, new UserAccounts.AccountChanges("John", "Doe", "john@example.com", null, null));

        verify(identityProvider, never()).replaceRoles(anyString(), any());
        assertEquals(Set.of(employeeRole), user.getRoles());
    }

    @Test
    void update_WhenPasswordGiven_ShouldResetItInKeycloak() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        accounts.update(1L, new UserAccounts.AccountChanges("John", "Doe", "john@example.com", "newPassword123", null));

        verify(identityProvider).resetPassword("kc-john", "newPassword123");
    }

    @Test
    void update_WhenPasswordBlank_ShouldNotResetIt() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        accounts.update(1L, new UserAccounts.AccountChanges("John", "Doe", "john@example.com", "   ", null));

        verify(identityProvider, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void update_WhenEmailTakenByAnother_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("different@example.com")).thenReturn(true);
        UserAccounts.AccountChanges changes =
                new UserAccounts.AccountChanges("John", "Doe", "different@example.com", null, null);

        ApiException exception = assertThrows(ApiException.class, () -> accounts.update(1L, changes));

        assertEquals("errors.user.emailTaken", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(identityProvider, never()).updateUser(anyString(), any());
    }

    @Test
    void update_WhenNoSuchAccount_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserAccounts.AccountChanges changes =
                new UserAccounts.AccountChanges("John", "Doe", "john@example.com", null, null);

        ApiException exception = assertThrows(ApiException.class, () -> accounts.update(1L, changes));

        assertEquals("errors.user.notFound", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void deactivate_ShouldDisableTheKeycloakAccountAndMarkItInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        accounts.deactivate(1L);

        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isActive());
        verify(identityProvider).disable("kc-john");
    }
}
