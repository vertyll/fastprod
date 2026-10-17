package com.vertyll.fastprod.shared.i18n;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlLangListenerTest {

    @Test
    void aCookieSharedWithKeycloakLivesUnderTheParentDomain() {
        assertThat(HtmlLangListener.sharedDomain("fastprod.vertyll.dev")).contains("vertyll.dev");
    }

    @Test
    void localhostAndAnAddressShareCookiesWithoutADomain() {
        assertThat(HtmlLangListener.sharedDomain("localhost")).isEmpty();
        assertThat(HtmlLangListener.sharedDomain(InetAddress.getLoopbackAddress().getHostAddress())).isEmpty();
    }
}
