package com.vertyll.fastprod.shared.i18n;

import java.io.Serial;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;
import com.vaadin.flow.i18n.I18NProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BackendI18NProvider implements I18NProvider {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final Locale POLISH = Locale.forLanguageTag("pl");
    public static final Locale ENGLISH = Locale.ENGLISH;

    private static final List<Locale> LOCALES = List.of(POLISH, ENGLISH);

    private final transient TranslationCatalogues catalogues;

    public BackendI18NProvider(TranslationCatalogues catalogues) {
        this.catalogues = catalogues;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return LOCALES;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        String pattern = catalogues.messages(language(locale)).get(key);
        if (pattern == null) {
            return key;
        }
        try {
            MessageFormat format = new MessageFormat(pattern, locale);
            if (params.length == 1 && params[0] instanceof Map<?, ?> named) {
                return format.format(named);
            }
            return format.format(params);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid translation {} for {}", key, locale, e);
            return pattern;
        }
    }

    private static String language(Locale locale) {
        return LOCALES.stream()
            .filter(candidate -> candidate.getLanguage().equals(locale.getLanguage()))
            .findFirst()
            .orElse(POLISH)
            .getLanguage();
    }
}
