package com.vertyll.fastprod.shared.security;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.exception.ApiException;

import com.vaadin.flow.server.VaadinSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityService {

    private final AuthService authService;

    private static final String USER_SESSION_KEY = "user";
    private static final String TOKEN_SESSION_KEY = "token";
    private static final String TOKEN_TYPE_SESSION_KEY = "token_type";
    private static final String ROLES_SESSION_KEY = "roles";

    public void login(AuthResponseDto authResponse) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(TOKEN_SESSION_KEY, authResponse.token());
            session.setAttribute(TOKEN_TYPE_SESSION_KEY, authResponse.type());
            session.setAttribute(USER_SESSION_KEY, authResponse);

            List<String> roles = JwtParser.extractRoles(authResponse.token());
            session.setAttribute(ROLES_SESSION_KEY, roles);

            log.debug("User logged in with token type: {} and roles: {}", authResponse.type(), roles);
        }
    }

    public void logout() {
        try {
            authService.logout();
            log.debug("Backend logout successful");
        } catch (ApiException e) {
            log.error("Failed to logout from backend, proceeding with local session cleanup", e);
        } finally {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.setAttribute(TOKEN_SESSION_KEY, null);
                session.setAttribute(TOKEN_TYPE_SESSION_KEY, null);
                session.setAttribute(USER_SESSION_KEY, null);
                session.setAttribute(ROLES_SESSION_KEY, null);
                session.close();
            }
        }
    }

    public boolean isAuthenticated() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null && session.getAttribute(TOKEN_SESSION_KEY) != null;
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(VaadinSession.getCurrent())
            .map(session -> (String) session.getAttribute(TOKEN_SESSION_KEY));
    }

    public List<String> getCurrentUserRoles() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            @SuppressWarnings("unchecked") List<String> roles = (List<String>) session.getAttribute(ROLES_SESSION_KEY);
            return roles != null ? roles : List.of();
        }
        return List.of();
    }

    public boolean hasRole(RoleType role) {
        List<String> userRoles = getCurrentUserRoles();
        String roleName = role.name();
        String roleAuthority = role.getAuthority();
        return userRoles.contains(roleName) || userRoles.contains(roleAuthority);
    }

    public boolean hasAnyRole(RoleType... roles) {
        for (RoleType role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
