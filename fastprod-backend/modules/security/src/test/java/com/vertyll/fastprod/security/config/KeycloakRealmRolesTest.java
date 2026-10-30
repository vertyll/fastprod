package com.vertyll.fastprod.security.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmRolesTest {
    private static Jwt token(Map<String, Object> claims) {
        return new Jwt(
            "token",
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T11:00:00Z"),
            Map.of("alg", "RS256"),
            claims
        );
    }

    @Test
    void realmRolesBecomeAuthorities() {
        Jwt jwt = token(Map.of("realm_access", Map.of("roles", List.of("ADMIN", "USER"))));

        List<String> authorities =
                new KeycloakRealmRoles().convert(jwt).stream().map(GrantedAuthority::getAuthority).toList();

        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), authorities);
    }

    @Test
    void aTokenWithoutRealmAccessGivesNoAuthorities() {
        assertTrue(new KeycloakRealmRoles().convert(token(Map.of("sub", "subject"))).isEmpty());
    }

    @Test
    void realmAccessWithoutARoleListGivesNoAuthorities() {
        Jwt jwt = token(Map.of("realm_access", Map.of("roles", "ADMIN")));

        assertTrue(new KeycloakRealmRoles().convert(jwt).isEmpty());
    }
}
