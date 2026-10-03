package com.vertyll.fastprod.user.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.vertyll.fastprod.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "errors.user.notFound";
    private static final String EMAIL_MISSING = "errors.auth.authenticationRequired";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final IdentityProvider identityProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto currentUser(Jwt token) {
        String email = token.getClaimAsString(StandardClaimNames.EMAIL);
        if (email == null || email.isBlank()) {
            throw new ApiException(EMAIL_MISSING, HttpStatus.UNAUTHORIZED);
        }
        String givenName = token.getClaimAsString(StandardClaimNames.GIVEN_NAME);
        String familyName = token.getClaimAsString(StandardClaimNames.FAMILY_NAME);
        String firstName = givenName != null ? givenName : email;
        String lastName = familyName != null ? familyName : "";
        boolean emailVerified = Boolean.TRUE.equals(token.getClaimAsBoolean(StandardClaimNames.EMAIL_VERIFIED));
        Set<Role> roles = realmRoles(token).stream().map(roleService::getOrCreateDefaultRole).collect(Collectors.toSet());

        String keycloakId = subject(token);
        User user = userRepository.findByKeycloakIdWithRoles(keycloakId)
            .orElseGet(
                () -> User.builder()
                    .keycloakId(keycloakId)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .verified(emailVerified)
                    .build()
            );
        user.syncIdentity(email, firstName, lastName, emailVerified);
        user.assignRoles(roles);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto updateCurrentUserProfile(Jwt token, ProfileUpdateDto dto) {
        User user = userRepository.findByKeycloakIdWithRoles(subject(token))
            .orElseThrow(() -> new ApiException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
        identityProvider.rename(user.getKeycloakId(), dto.firstName(), dto.lastName());
        user.rename(dto.firstName(), dto.lastName());
        return userMapper.toResponseDto(userRepository.save(user));
    }

    private static List<RoleType> realmRoles(Jwt token) {
        Map<String, Object> realmAccess = token.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of(RoleType.USER);
        }
        return roles.stream()
            .map(String::valueOf)
            .filter(name -> Arrays.stream(RoleType.values()).anyMatch(role -> role.getValue().equals(name)))
            .map(RoleType::fromValue)
            .toList();
    }

    private static String subject(Jwt token) {
        return Objects.requireNonNull(token.getSubject(), "A Keycloak access token always carries a subject");
    }
}
