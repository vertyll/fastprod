package com.vertyll.fastprod.base.ui.component;

import java.io.Serial;
import java.util.Locale;

import com.vertyll.fastprod.shared.i18n.BackendI18NProvider;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.VaadinSession;

public final class LanguageSwitcher extends Select<Locale> {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String LANGUAGE_ATTRIBUTE = "fastprod.language";

    public LanguageSwitcher() {
        super();
        setItems(BackendI18NProvider.POLISH, BackendI18NProvider.ENGLISH);
        setItemLabelGenerator(locale -> I18n.t("language." + locale.getLanguage()));
        setValue(matching(I18n.locale()));
        setAriaLabel(I18n.t("language.label"));
        setWidth("8rem");
        addValueChangeListener(event -> {
            if (event.isFromClient()) {
                VaadinSession session = VaadinSession.getCurrent();
                session.setLocale(event.getValue());
                session.getSession().setAttribute(LANGUAGE_ATTRIBUTE, event.getValue().getLanguage());
                UI.getCurrent().getPage().reload();
            }
        });
    }

    private static Locale matching(Locale current) {
        return BackendI18NProvider.ENGLISH.getLanguage().equals(current.getLanguage()) ? BackendI18NProvider.ENGLISH
                : BackendI18NProvider.POLISH;
    }
}
