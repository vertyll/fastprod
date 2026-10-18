package com.vertyll.fastprod.shared.security;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.jspecify.annotations.Nullable;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;

/**
 * The view a sign-in started from a button returns to, named in {@code return_to} and kept in the session while Keycloak
 * signs the person in. Only a path on the application's own site is kept, so the sign-in cannot send the browser
 * elsewhere. A view that needs a session is returned to by Spring Security's own saved request, which this cache keeps
 * doing for every other sign-in.
 */
final class ReturnPage implements RequestCache {

    static final String PARAMETER = "return_to";

    private static final String ATTRIBUTE = ReturnPage.class.getName();

    private static final int MOST_LENGTH = 2048;

    private final RequestCache sessions = new HttpSessionRequestCache();

    /** Keeps the view this sign-in names, or forgets an earlier one when it names none on this site. */
    static void remember(HttpServletRequest request) {
        Optional<String> page = onThisSite(request.getParameter(PARAMETER));
        if (page.isPresent()) {
            request.getSession().setAttribute(ATTRIBUTE, page.get());
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ATTRIBUTE);
        }
    }

    /** A path that starts with a single {@code /}, with no backslash and no control character. */
    static Optional<String> onThisSite(@Nullable String path) {
        return Optional.ofNullable(path)
            .filter(
                candidate -> candidate.startsWith("/") && !candidate.startsWith("//") && candidate.indexOf('\\') < 0
                        && candidate.chars().noneMatch(Character::isISOControl) && candidate.length() <= MOST_LENGTH
            );
    }

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        sessions.saveRequest(request, response);
    }

    /** The view a button's sign-in named, once, else the request Spring Security saved. */
    @Override
    public @Nullable SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Object page = session == null ? null : session.getAttribute(ATTRIBUTE);
        if (session != null && page instanceof String path && onThisSite(path).isPresent()) {
            session.removeAttribute(ATTRIBUTE);
            return new SimpleSavedRequest(path);
        }
        return sessions.getRequest(request, response);
    }

    @Override
    public @Nullable HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
        return sessions.getMatchingRequest(request, response);
    }

    @Override
    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
        sessions.removeRequest(request, response);
    }
}
