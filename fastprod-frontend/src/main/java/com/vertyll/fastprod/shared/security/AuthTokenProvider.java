package com.vertyll.fastprod.shared.security;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Component;

/**
 * Provides authentication token from current Vaadin session.
 * Thread-safe as VaadinSession is bound to the current request thread.
 */
@Component
public class AuthTokenProvider {

    private static final String TOKEN_SESSION_KEY = "token";
    private static final String TOKEN_TYPE_SESSION_KEY = "token_type";

    /**
     * Gets the authentication token from the current Vaadin session.
     *
     * @return authentication token or null if not authenticated
     */
    public String getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return (String) session.getAttribute(TOKEN_SESSION_KEY);
        }
        return null;
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
     * @return authorization header value or null if not authenticated
     */
    public String getAuthorizationHeader() {
        String token = getToken();
        if (token != null) {
            return getTokenType() + " " + token;
        }
        return null;
    }
}

