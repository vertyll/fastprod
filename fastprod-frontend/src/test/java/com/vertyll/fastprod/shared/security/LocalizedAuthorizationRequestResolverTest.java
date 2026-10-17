package com.vertyll.fastprod.shared.security;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedAuthorizationRequestResolverTest {
    private static final ClientRegistration REGISTRATION = ClientRegistration.withRegistrationId("keycloak")
        .clientId("fastprod-frontend")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://localhost/callback")
        .authorizationUri("http://localhost/auth")
        .tokenUri("http://localhost/token")
        .build();

    private final LocalizedAuthorizationRequestResolver resolver =
            new LocalizedAuthorizationRequestResolver(new InMemoryClientRegistrationRepository(REGISTRATION));

    @Test
    void anAccountActionIsPassedOnToKeycloak() {
        OAuth2AuthorizationRequest authorization = resolve("CONFIGURE_TOTP");

        assertThat(authorization.getAdditionalParameters()).containsEntry("kc_action", "CONFIGURE_TOTP");
    }

    @Test
    void anUnknownAccountActionIsDropped() {
        OAuth2AuthorizationRequest authorization = resolve("delete_account");

        assertThat(authorization.getAdditionalParameters()).doesNotContainKey("kc_action");
    }

    private OAuth2AuthorizationRequest resolve(String action) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", SecurityConfig.LOGIN_PATH);
        request.setServletPath(SecurityConfig.LOGIN_PATH);
        request.setParameter(LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER, action);
        return Objects.requireNonNull(resolver.resolve(request), "a sign-in request resolves");
    }
}
