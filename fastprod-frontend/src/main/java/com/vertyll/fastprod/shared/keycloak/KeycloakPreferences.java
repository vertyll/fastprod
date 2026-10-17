package com.vertyll.fastprod.shared.keycloak;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.shared.config.KeycloakProperties;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.spring.annotation.SpringComponent;

/**
 * The language the reader last chose, in the application or on Keycloak's pages, kept in the cookie
 * {@code ui_locale_<realm>}, which both write: a shared record rather than a message, so the two agree however the reader
 * moves between them. It lives under the domain the application shares with Keycloak, the parent of Keycloak's host.
 */
@SpringComponent
public class KeycloakPreferences implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int ONE_YEAR = 60 * 60 * 24 * 365;

    private final String cookieName;
    private final @Nullable String cookieDomain;
    private final transient KeycloakAccount account;

    KeycloakPreferences(KeycloakProperties keycloak, KeycloakAccount account) {
        this.cookieName = "ui_locale_" + keycloak.realm();
        this.cookieDomain = sharedDomain(URI.create(keycloak.realmUrl()).getHost()).orElse(null);
        this.account = account;
    }

    /** The language last chosen here or on Keycloak's pages, as a language tag such as {@code pl}, when one was. */
    public Optional<String> chosenLanguage(VaadinRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    /**
     * Writes the language to the shared cookie, and, for a signed-in reader, to their Keycloak account, which Keycloak's
     * pages show a signed-in person in.
     */
    public void languageChosen(UI ui, String language) {
        String domain = cookieDomain == null ? "" : "; Domain=" + cookieDomain;
        ui.getPage()
            .executeJs(
                "document.cookie = $0 + (location.protocol === 'https:' ? '; Secure' : '')",
                cookieName + "=" + language + "; Path=/; Max-Age=" + ONE_YEAR + "; SameSite=Lax" + domain
            );
        account.keepLanguage(language);
    }

    /**
     * The domain a cookie shared with Keycloak lives under: the parent of Keycloak's host. {@code localhost} and an IP
     * address have none, as every port there already shares cookies.
     */
    static Optional<String> sharedDomain(String host) {
        String[] labels = host.split("\\.", -1);
        boolean isAddress = Arrays.stream(labels).allMatch(label -> label.chars().allMatch(Character::isDigit));
        return labels.length > 2 && !isAddress ? Optional.of(host.substring(host.indexOf('.') + 1)) : Optional.empty();
    }
}
