package com.vertyll.fastprod.shared.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.vaadin.flow.server.VaadinSession;

/**
 * Provides authentication token from current Vaadin session. Thread-safe as VaadinSession is bound
 * to the current request thread.
 */
@Component
public class AuthTokenProvider {

    private static final String TOKEN_SESSION_KEY = "token";
    private static final String TOKEN_TYPE_SESSION_KEY = "token_type";

    /**
     * Gets the authentication token from the current Vaadin session.
     *
     * @return authentication token, or empty if not authenticated
     */
    public Optional<String> getToken() {
        return Optional.ofNullable(VaadinSession.getCurrent())
            .map(session -> (String) session.getAttribute(TOKEN_SESSION_KEY));
    }

    /**
     * Gets the token type (e.g., "Bearer") from the current Vaadin session.
     *
     * @return token type or "Bearer" as default
     */
    public String getTokenType() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            String type = (String) session.getAttribute(TOKEN_TYPE_SESSION_KEY);
            return type != null ? type : "Bearer";
        }
        return "Bearer";
    }

    /**
     * Gets the full Authorization header value (e.g., "Bearer eyJhbGc...").
     *
     * @return authorization header value, or empty if not authenticated
     */
    public Optional<String> getAuthorizationHeader() {
        return getToken().map(token -> getTokenType() + " " + token);
    }
}
