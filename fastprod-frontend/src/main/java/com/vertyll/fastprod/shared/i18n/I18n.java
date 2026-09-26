package com.vertyll.fastprod.shared.i18n;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.vertyll.fastprod.shared.exception.ApiException;

import com.vaadin.flow.component.UI;

public final class I18n {

    private static final String UNEXPECTED_ERROR = "errors.common.unexpected";

    private I18n() {
    }

    public static String t(String key, Object... params) {
        UI ui = UI.getCurrent();
        return ui == null ? key : ui.getTranslation(key, params);
    }

    public static String t(String key, Map<String, ?> args) {
        UI ui = UI.getCurrent();
        return ui == null ? key : ui.getTranslation(key, args);
    }

    public static String error(ApiException exception) {
        String translated = t(exception.getMessage(), exception.getArgs());
        return translated.equals(exception.getMessage()) ? t(UNEXPECTED_ERROR) : translated;
    }

    public static String role(String roleName) {
        return t("roles." + roleName);
    }

    public static String roles(Collection<String> roleNames) {
        return String.join(", ", roleNames.stream().map(I18n::role).toList());
    }

    public static Locale locale() {
        UI ui = UI.getCurrent();
        return ui == null ? BackendI18NProvider.POLISH : ui.getLocale();
    }
}
