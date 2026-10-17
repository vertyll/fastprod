package com.vertyll.fastprod.base.ui.component;

import java.io.Serial;
import java.util.Locale;

import com.vertyll.fastprod.shared.i18n.BackendI18NProvider;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.keycloak.KeycloakPreferences;
import com.vertyll.fastprod.shared.security.SessionLanguage;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.VaadinSession;

public final class LanguageSwitcher extends Select<Locale> {
    @Serial
    private static final long serialVersionUID = 1L;

    public LanguageSwitcher(KeycloakPreferences keycloakPreferences) {
        super();
        setItems(BackendI18NProvider.POLISH, BackendI18NProvider.ENGLISH);
        setItemLabelGenerator(locale -> locale.getLanguage().toUpperCase(Locale.ROOT));
        setValue(matching(I18n.locale()));
        setAriaLabel(I18n.t("language.label"));
        setWidth("5rem");
        addValueChangeListener(event -> {
            if (event.isFromClient()) {
                SessionLanguage.use(VaadinSession.getCurrent(), event.getValue());
                keycloakPreferences.languageChosen(UI.getCurrent(), event.getValue().getLanguage());
                UI.getCurrent().getPage().reload();
            }
        });
    }

    private static Locale matching(Locale current) {
        return BackendI18NProvider.ENGLISH.getLanguage().equals(current.getLanguage()) ? BackendI18NProvider.ENGLISH
                : BackendI18NProvider.POLISH;
    }
}
