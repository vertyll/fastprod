package com.vertyll.fastprod.shared.security;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpSession;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.server.VaadinSession;

/**
 * The language a reader's session shows, kept where the sign-in reads it too, so the sign-in opens Keycloak's pages in
 * the same language.
 */
public final class SessionLanguage {

    private static final String ATTRIBUTE = "fastprod.language";

    /** A language subtag, so nothing else read from the session reaches the sign-in's redirect. */
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2,8}");

    private SessionLanguage() {
    }

    /** Shows this session in the language of the given locale, and keeps it for the next sign-in. */
    public static void use(VaadinSession session, Locale locale) {
        session.setLocale(locale);
        session.getSession().setAttribute(ATTRIBUTE, locale.getLanguage());
    }

    /** The language the session shows, as a language tag, when the reader chose one. */
    static Optional<String> of(@Nullable HttpSession session) {
        return Optional.ofNullable(session)
            .map(chosen -> chosen.getAttribute(ATTRIBUTE))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(language -> LANGUAGE.matcher(language).matches());
    }
}
