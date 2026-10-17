package com.vertyll.fastprod.shared.security;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
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
    void removingACredentialNamedByItsIdIsPassedOnToKeycloak() {
        String action = "delete_credential:90062308-97cf-410a-afe5-06680a07a349";

        assertThat(resolve(action).getAdditionalParameters()).containsEntry("kc_action", action);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "delete_account",
            "delete_credential",
            "delete_credential:otp"
        }
    )
    void anUnknownAccountActionIsDropped(String action) {
        OAuth2AuthorizationRequest authorization = resolve(action);

        assertThat(authorization.getAdditionalParameters()).doesNotContainKey("kc_action");
    }

    @Test
    void theSignInOpensInTheLanguageTheSessionShows() {
        MockHttpServletRequest request = signIn();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("fastprod.language", "en");
        request.setSession(session);

        assertThat(resolved(request).getAdditionalParameters()).containsEntry("ui_locales", "en");
    }

    @Test
    void aSessionWithoutALanguageOpensInTheDefaultOne() {
        assertThat(resolved(signIn()).getAdditionalParameters()).containsEntry("ui_locales", "pl");
    }

    @Test
    void aValueThatIsNoLanguageNeverReachesKeycloak() {
        MockHttpServletRequest request = signIn();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("fastprod.language", "en&prompt=none");
        request.setSession(session);

        assertThat(resolved(request).getAdditionalParameters()).containsEntry("ui_locales", "pl");
    }

    private static MockHttpServletRequest signIn() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", SecurityConfig.LOGIN_PATH);
        request.setServletPath(SecurityConfig.LOGIN_PATH);
        return request;
    }

    private OAuth2AuthorizationRequest resolved(MockHttpServletRequest request) {
        return Objects.requireNonNull(resolver.resolve(request), "a sign-in request resolves");
    }

    private OAuth2AuthorizationRequest resolve(String action) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", SecurityConfig.LOGIN_PATH);
        request.setServletPath(SecurityConfig.LOGIN_PATH);
        request.setParameter(LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER, action);
        return Objects.requireNonNull(resolver.resolve(request), "a sign-in request resolves");
    }
}
