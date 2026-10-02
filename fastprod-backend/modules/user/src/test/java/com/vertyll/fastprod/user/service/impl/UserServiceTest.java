package com.vertyll.fastprod.user.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.oauth2.jwt.Jwt;

import com.vertyll.fastprod.role.entity.Role;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.identity.IdentityProvider;
import com.vertyll.fastprod.user.mapper.UserMapper;
import com.vertyll.fastprod.user.repository.UserRepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Test class: userMapper is used by Mockito injection")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private IdentityProvider identityProvider;

    @Spy
    @SuppressWarnings("UnusedVariable")
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private Role userRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name(RoleType.USER).description("User role").build();
        managerRole = Role.builder().name(RoleType.MANAGER).description("Manager role").build();
    }

    @Test
    void currentUser_WhenFirstSignIn_ShouldCreateAccountFromToken() {
        when(userRepository.findByKeycloakIdWithRoles("kc-1")).thenReturn(Optional.empty());
        when(roleService.getOrCreateDefaultRole(RoleType.USER)).thenReturn(userRole);
        when(roleService.getOrCreateDefaultRole(RoleType.MANAGER)).thenReturn(managerRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result =
                userService.currentUser(token("kc-1", "jan@example.com", true, "USER", "MANAGER", "offline_access"));

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("kc-1", saved.getKeycloakId());
        assertEquals("jan@example.com", saved.getEmail());
        assertEquals("Jan", saved.getFirstName());
        assertEquals("Kowalski", saved.getLastName());
        assertTrue(saved.isVerified());
        assertEquals(Set.of(RoleType.USER, RoleType.MANAGER), result.roles());
    }

    @Test
    void currentUser_WhenAccountExists_ShouldFollowTheToken() {
        User existing = User.builder()
            .keycloakId("kc-1")
            .firstName("Stare")
            .lastName("Imię")
            .email("stary@example.com")
            .roles(Set.of(managerRole))
            .verified(false)
            .build();
        when(userRepository.findByKeycloakIdWithRoles("kc-1")).thenReturn(Optional.of(existing));
        when(roleService.getOrCreateDefaultRole(RoleType.USER)).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result = userService.currentUser(token("kc-1", "jan@example.com", true, "USER"));

        assertEquals("jan@example.com", result.email());
        assertEquals(Set.of(RoleType.USER), result.roles());
        assertTrue(result.isVerified());
    }

    @Test
    void currentUser_WhenTokenHasNoEmail_ShouldRefuse() {
        Jwt token = Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject("kc-1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        ApiException exception = assertThrows(ApiException.class, () -> userService.currentUser(token));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateCurrentUserProfile_ShouldRenameInKeycloakAndLocally() {
        User existing = User.builder()
            .keycloakId("kc-1")
            .firstName("Jan")
            .lastName("Kowalski")
            .email("jan@example.com")
            .verified(true)
            .build();
        when(userRepository.findByKeycloakIdWithRoles("kc-1")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result = userService.updateCurrentUserProfile(
            token("kc-1", "jan@example.com", true, "USER"),
            new ProfileUpdateDto("Janusz", "Nowak")
        );

        verify(identityProvider).rename("kc-1", "Janusz", "Nowak");
        assertEquals("Janusz", result.firstName());
        assertEquals("Nowak", result.lastName());
    }

    @Test
    void updateCurrentUserProfile_WhenAccountMissing_ShouldThrowNotFound() {
        when(userRepository.findByKeycloakIdWithRoles("kc-1")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
            ApiException.class,
            () -> userService
                .updateCurrentUserProfile(token("kc-1", "jan@example.com", false), new ProfileUpdateDto("Jan", "Nowak"))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertFalse(exception.getMessage().isBlank());
        verify(identityProvider, never()).rename(any(), any(), any());
    }

    private static Jwt token(String subject, String email, boolean verified, String... roles) {
        return Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject(subject)
            .claim("email", email)
            .claim("email_verified", verified)
            .claim("given_name", "Jan")
            .claim("family_name", "Kowalski")
            .claim("realm_access", Map.of("roles", List.of(roles)))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    }
}
