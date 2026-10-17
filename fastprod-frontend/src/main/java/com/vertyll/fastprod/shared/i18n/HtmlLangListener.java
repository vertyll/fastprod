package com.vertyll.fastprod.shared.i18n;

import java.io.Serial;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.http.Cookie;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.base.ui.component.LanguageSwitcher;
import com.vertyll.fastprod.shared.config.KeycloakProperties;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;

/**
 * Sets each page's language: the one chosen on Keycloak's pages when the sign-in brings one back in the cookie
 * {@code ui_locale_<realm>}, which is then taken over and removed, else the reader's.
 */
@SpringComponent
public class HtmlLangListener implements VaadinServiceInitListener {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String signInLanguageCookie;

    public HtmlLangListener(KeycloakProperties keycloak) {
        this.signInLanguageCookie = "ui_locale_" + keycloak.realm();
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            takeSignInLanguage(ui);
            ui.getPage().executeJs("document.documentElement.lang = $0", ui.getLocale().getLanguage());
        });
    }

    private void takeSignInLanguage(UI ui) {
        VaadinRequest request = VaadinRequest.getCurrent();
        VaadinResponse response = VaadinResponse.getCurrent();
        String handedOver = request == null ? null : cookie(request, signInLanguageCookie);
        if (handedOver == null || response == null) {
            return;
        }
        supported(handedOver).ifPresent(locale -> {
            VaadinSession session = ui.getSession();
            session.setLocale(locale);
            session.getSession().setAttribute(LanguageSwitcher.LANGUAGE_ATTRIBUTE, locale.getLanguage());
            ui.setLocale(locale);
        });
        Cookie removal = new Cookie(signInLanguageCookie, "");
        removal.setPath("/");
        removal.setMaxAge(0);
        removal.setHttpOnly(true);
        removal.setSecure(request.isSecure());
        sharedDomain(serverName(request)).ifPresent(removal::setDomain);
        response.addCookie(removal);
    }

    private static Optional<Locale> supported(String language) {
        return BackendI18NProvider.LOCALES.stream().filter(locale -> locale.getLanguage().equals(language)).findFirst();
    }

    private static String serverName(VaadinRequest request) {
        String host = request.getHeader("Host");
        if (host == null) {
            return "";
        }
        int port = host.indexOf(':');
        return port < 0 ? host : host.substring(0, port);
    }

    /**
     * The domain a cookie shared with Keycloak lives under: the parent of this host. {@code localhost} and an IP
     * address have none, as every port there already shares cookies.
     */
    static Optional<String> sharedDomain(String host) {
        String[] labels = host.split("\\.", -1);
        boolean isAddress = Arrays.stream(labels).allMatch(label -> label.chars().allMatch(Character::isDigit));
        return labels.length > 2 && !isAddress ? Optional.of(host.substring(host.indexOf('.') + 1)) : Optional.empty();
    }

    private static @Nullable String cookie(VaadinRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(cookie -> name.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
