package com.vertyll.fastprod.security.config;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class TokenValidatorTest {
    private static final String ISSUER = "https://keycloak.example/realms/fastprod";

    private final OAuth2TokenValidator<Jwt> validator =
            SecurityConfig.tokenValidator(new KeycloakResourceServerProperties(ISSUER, null, "fastprod-api"));

    @Test
    void acceptsATokenIssuedForThisApi() {
        assertThat(validator.validate(token(ISSUER, List.of("fastprod-api", "account"))).hasErrors()).isFalse();
    }

    @Test
    void rejectsATokenIssuedForAnotherClient() {
        assertThat(validator.validate(token(ISSUER, List.of("account"))).hasErrors()).isTrue();
    }

    @Test
    void rejectsATokenFromAnotherIssuer() {
        assertThat(
            validator.validate(token("https://other.example/realms/fastprod", List.of("fastprod-api"))).hasErrors()
        ).isTrue();
    }

    private static Jwt token(String issuer, List<String> audience) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .issuer(issuer)
            .audience(audience)
            .subject("user")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }
}
