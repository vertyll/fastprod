package com.vertyll.fastprod.shared.security;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.communication.IndexHtmlResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentSecurityPolicyTest {
    private static final Pattern NONCE = Pattern.compile("'nonce-([^']+)'");

    @Test
    void aNonceHas128RandomBits() {
        String nonce = ContentSecurityPolicy.newNonce();

        assertThat(Base64.getDecoder().decode(nonce)).hasSize(16);
        assertThat(ContentSecurityPolicy.newNonce()).isNotEqualTo(nonce);
    }

    @Test
    void inProductionEveryScriptOfThePageCarriesTheNonceOfItsPolicy() {
        Document page = Jsoup.parse(
            "<html><head><script>window.Vaadin = {}</script>"
                    + "<script type=\"module\" src=\"./VAADIN/build/indexhtml.js\"></script></head><body></body></html>"
        );

        String policy = policyOf(page, true);

        Matcher nonce = NONCE.matcher(policy);
        assertThat(nonce.find()).isTrue();
        assertThat(page.getElementsByTag("script")).hasSize(2)
            .allSatisfy(script -> assertThat(script.attr("nonce")).isEqualTo(nonce.group(1)));
        assertThat(scriptSources(policy)).doesNotContain("'unsafe-inline'").contains("'strict-dynamic'");
    }

    @Test
    void eachPageGetsANonceOfItsOwn() {
        String first = policyOf(Jsoup.parse("<script></script>"), true);
        String second = policyOf(Jsoup.parse("<script></script>"), true);

        assertThat(nonceOf(first)).isNotEqualTo(nonceOf(second));
    }

    @Test
    void inDevelopmentThePageNeedsNoNonce() {
        Document page = Jsoup.parse("<script></script>");

        String policy = policyOf(page, false);

        assertThat(policy).doesNotContain("'nonce-");
        assertThat(page.getElementsByTag("script")).noneSatisfy(script -> assertThat(script.hasAttr("nonce")).isTrue());
    }

    @Test
    void anyOtherResponseRunsScriptsOnlyFromTheApplication() {
        String policy = ContentSecurityPolicy.forOtherResponses();

        assertThat(scriptSources(policy)).isEqualTo("'self'");
        assertThat(policy).contains("object-src 'none'", "frame-ancestors 'none'", "base-uri 'self'");
    }

    private static String policyOf(Document page, boolean productionMode) {
        VaadinService service = mock(VaadinService.class);
        DeploymentConfiguration configuration = mock(DeploymentConfiguration.class);
        when(service.getDeploymentConfiguration()).thenReturn(configuration);
        when(configuration.isProductionMode()).thenReturn(productionMode);
        ServiceInitEvent event = new ServiceInitEvent(service);
        new ContentSecurityPolicy().serviceInit(event);
        VaadinResponse response = mock(VaadinResponse.class);

        event.getAddedIndexHtmlRequestListeners()
            .forEach(
                listener -> listener
                    .modifyIndexHtmlResponse(new IndexHtmlResponse(mock(VaadinRequest.class), response, page))
            );

        ArgumentCaptor<String> policy = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(ContentSecurityPolicy.HEADER), policy.capture());
        return policy.getValue();
    }

    private static String nonceOf(String policy) {
        Matcher nonce = NONCE.matcher(policy);
        assertThat(nonce.find()).isTrue();
        return nonce.group(1);
    }

    private static String scriptSources(String policy) {
        return policy.substring(policy.indexOf("script-src ") + "script-src ".length());
    }
}
