package com.vertyll.fastprod.shared.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.vaadin.flow.server.VaadinSession;

@Component
public class AuthTokenProvider {
    private static final String TOKEN_SESSION_KEY = "token";
    private static final String TOKEN_TYPE_SESSION_KEY = "token_type";

    public Optional<String> getToken() {
        return Optional.ofNullable(VaadinSession.getCurrent())
            .map(session -> (String) session.getAttribute(TOKEN_SESSION_KEY));
    }

    public String getTokenType() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            String type = (String) session.getAttribute(TOKEN_TYPE_SESSION_KEY);
            return type != null ? type : "Bearer";
        }
        return "Bearer";
    }

    public Optional<String> getAuthorizationHeader() {
        return getToken().map(token -> getTokenType() + " " + token);
    }
}
