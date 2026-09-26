package com.vertyll.fastprod.shared.security;

import java.io.Serial;

import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SecurityBeforeEnterListener implements BeforeEnterListener {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String LOGIN_ROUTE = "login";

    private final transient SecurityService securityService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isAuthenticated = securityService.isAuthenticated();
        String targetLocation = event.getLocation().getPath();

        log.debug("Navigation to: {}, authenticated: {}", targetLocation, isAuthenticated);

        if (isAuthenticated) {
            guardAuthenticated(event, targetLocation);
        } else if (!isPublicRoute(targetLocation)) {
            log.info("Unauthorized access attempt to: {}. Redirecting to login.", targetLocation);
            event.rerouteTo(LOGIN_ROUTE);
        }
    }

    private void guardAuthenticated(BeforeEnterEvent event, String targetLocation) {
        if (isGuestOnlyRoute(targetLocation)) {
            log.info("Already authenticated. Redirecting to home.");
            event.rerouteTo("");
        } else if (isForbidden(targetLocation)) {
            log.warn("Access denied to {} for user without required roles", targetLocation);
            Notification notification =
                    Notification.show(I18n.t("errors.auth.pageForbidden"), 5000, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.rerouteTo("");
        }
    }

    private static boolean isPublicRoute(String location) {
        return location.isEmpty() || isGuestOnlyRoute(location) || "verify-account".equals(location)
                || location.startsWith("verify-account/") || "forgot-password".equals(location)
                || location.startsWith("reset-password");
    }

    private static boolean isGuestOnlyRoute(String location) {
        return LOGIN_ROUTE.equals(location) || "register".equals(location);
    }

    private boolean isForbidden(String location) {
        if (location.startsWith("admin/")) {
            return !securityService.hasRole(RoleType.ADMIN);
        }
        return location.startsWith("employees") && !securityService.hasAnyRole(RoleType.ADMIN, RoleType.MANAGER);
    }
}
