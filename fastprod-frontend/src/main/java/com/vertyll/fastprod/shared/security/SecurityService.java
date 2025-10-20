package com.vertyll.fastprod.shared.security;

import com.vaadin.flow.server.VaadinSession;
import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityService {

    private static final String USER_SESSION_KEY = "user";
    private static final String TOKEN_SESSION_KEY = "token";
    private static final String TOKEN_TYPE_SESSION_KEY = "token_type";

    public void login(AuthResponseDto authResponse) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(TOKEN_SESSION_KEY, authResponse.token());
            session.setAttribute(TOKEN_TYPE_SESSION_KEY, authResponse.type());
            session.setAttribute(USER_SESSION_KEY, authResponse);
            log.debug("User logged in with token type: {}", authResponse.type());
        }
    }

    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(TOKEN_SESSION_KEY, null);
            session.setAttribute(TOKEN_TYPE_SESSION_KEY, null);
            session.setAttribute(USER_SESSION_KEY, null);
            session.close();
        }
    }

    public boolean isAuthenticated() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return false;
        }
        return session.getAttribute(TOKEN_SESSION_KEY) != null;
    }

    public String getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return (String) session.getAttribute(TOKEN_SESSION_KEY);
        }
        return null;
    }

    public String getTokenType() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            String type = (String) session.getAttribute(TOKEN_TYPE_SESSION_KEY);
            return type != null ? type : "Bearer";
        }
        return "Bearer";
    }

    public String getAuthorizationHeader() {
        String token = getToken();
        if (token != null) {
            return getTokenType() + " " + token;
        }
        return null;
    }

    public AuthResponseDto getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return (AuthResponseDto) session.getAttribute(USER_SESSION_KEY);
        }
        return null;
    }
}
