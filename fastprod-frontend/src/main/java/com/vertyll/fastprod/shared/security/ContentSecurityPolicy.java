package com.vertyll.fastprod.shared.security;

import java.io.Serial;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.flow.spring.annotation.SpringComponent;

/**
 * The {@code Content-Security-Policy} of every response. In production the page Vaadin starts from gets a fresh nonce,
 * carried by each of its {@code script} tags, so only the scripts Vaadin put there run, and those load the rest; every
 * other response gets the same policy without one. In development the page allows inline scripts instead.
 */
@SpringComponent
public class ContentSecurityPolicy implements VaadinServiceInitListener {
    @Serial
    private static final long serialVersionUID = 1L;

    static final String HEADER = "Content-Security-Policy";

    private static final int NONCE_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Signing in and out navigate to Keycloak rather than submit a form, so no form leaves the origin. Vaadin's styles
    // draw component icons as data: SVG masks and Lumo's icons from a data: font.
    // Vaadin's components and Lumo add <style> elements and style attributes at run time, without a nonce, so styles
    // need 'unsafe-inline':
    // https://vaadin.com/docs/v25/flow/security/advanced-topics/frequent-issues#content-security-policy-csp-set-to-unsafe-values
    private static final List<String> RESTRICTIONS = List.of(
        "default-src 'self'",
        "object-src 'none'",
        "base-uri 'self'",
        "form-action 'self'",
        "frame-ancestors 'none'",
        "img-src 'self' data:",
        "font-src 'self' data:",
        "style-src 'self' 'unsafe-inline'"
    );

    @Override
    public void serviceInit(ServiceInitEvent event) {
        if (event.getSource().getDeploymentConfiguration().isProductionMode()) {
            event.addIndexHtmlRequestListener(ContentSecurityPolicy::withNonce);
        } else {
            event.addIndexHtmlRequestListener(ContentSecurityPolicy::forDevelopment);
        }
    }

    /** The policy of a response that is not Vaadin's page: it runs scripts only from the application itself. */
    static String forOtherResponses() {
        return withScripts("'self'");
    }

    static String forPage(String nonce) {
        // 'strict-dynamic' lets the nonced bootstrap load Vaadin's chunks; 'self' is the fallback of a browser without
        // it. Flow's client turns the JavaScript the server sends, executeJs included, into functions with
        // new Function(), which needs 'unsafe-eval':
        // https://vaadin.com/docs/v25/flow/security/advanced-topics/strict-csp#function-execution-eval-calls
        return withScripts("'self' 'nonce-" + nonce + "' 'strict-dynamic' 'unsafe-eval'");
    }

    /** A nonce of 128 random bits, base64-encoded, for one response. */
    static String newNonce() {
        byte[] nonce = new byte[NONCE_BYTES];
        RANDOM.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }

    private static void withNonce(IndexHtmlResponse page) {
        String nonce = newNonce();
        page.getDocument().getElementsByTag("script").attr("nonce", nonce);
        page.getVaadinResponse().setHeader(HEADER, forPage(nonce));
    }

    private static void forDevelopment(IndexHtmlResponse page) {
        // Vaadin supports the nonce in production mode only, and its development tools add inline scripts without one:
        // https://vaadin.com/docs/v25/flow/security/advanced-topics/strict-csp
        page.getVaadinResponse().setHeader(HEADER, withScripts("'self' 'unsafe-inline' 'unsafe-eval'"));
    }

    private static String withScripts(String sources) {
        return String.join("; ", RESTRICTIONS) + "; script-src " + sources;
    }
}
