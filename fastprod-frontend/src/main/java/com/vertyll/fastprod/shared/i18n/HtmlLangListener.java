package com.vertyll.fastprod.shared.i18n;

import java.io.Serial;
import java.util.Locale;
import java.util.Optional;

import com.vertyll.fastprod.base.ui.component.LanguageSwitcher;
import com.vertyll.fastprod.shared.keycloak.KeycloakPreferences;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;

/** Sets each page's language: the one the reader last chose, here or on Keycloak's pages, else the reader's. */
@SpringComponent
public class HtmlLangListener implements VaadinServiceInitListener {
    @Serial
    private static final long serialVersionUID = 1L;

    private final KeycloakPreferences keycloak;

    public HtmlLangListener(KeycloakPreferences keycloak) {
        this.keycloak = keycloak;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            VaadinRequest request = VaadinRequest.getCurrent();
            if (request != null) {
                keycloak.chosenLanguage(request)
                    .flatMap(HtmlLangListener::supported)
                    .filter(locale -> !locale.equals(ui.getLocale()))
                    .ifPresent(locale -> {
                        VaadinSession session = ui.getSession();
                        session.setLocale(locale);
                        session.getSession().setAttribute(LanguageSwitcher.LANGUAGE_ATTRIBUTE, locale.getLanguage());
                        ui.setLocale(locale);
                    });
            }
            ui.getPage().executeJs("document.documentElement.lang = $0", ui.getLocale().getLanguage());
        });
    }

    private static Optional<Locale> supported(String language) {
        return BackendI18NProvider.LOCALES.stream().filter(locale -> locale.getLanguage().equals(language)).findFirst();
    }
}
