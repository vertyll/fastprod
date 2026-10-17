package com.vertyll.fastprod.shared.keycloak;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakPreferencesTest {

    @Test
    void aCookieSharedWithKeycloakLivesUnderTheParentDomain() {
        assertThat(KeycloakPreferences.sharedDomain("fastprod.vertyll.dev")).contains("vertyll.dev");
    }

    @Test
    void localhostAndAnAddressShareCookiesWithoutADomain() {
        assertThat(KeycloakPreferences.sharedDomain("localhost")).isEmpty();
        assertThat(KeycloakPreferences.sharedDomain(InetAddress.getLoopbackAddress().getHostAddress())).isEmpty();
    }
}
