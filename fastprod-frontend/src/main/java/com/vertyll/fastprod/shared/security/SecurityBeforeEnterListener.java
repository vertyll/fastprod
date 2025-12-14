package com.vertyll.fastprod.shared.security;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SecurityBeforeEnterListener implements BeforeEnterListener {

    private final SecurityService securityService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isAuthenticated = securityService.isAuthenticated();
        String targetLocation = event.getLocation().getPath();

        log.debug("Navigation to: {}, authenticated: {}", targetLocation, isAuthenticated);

        // Public routes that don't require authentication
        boolean isPublicRoute = targetLocation.equals("login")
                || targetLocation.equals("register")
                || targetLocation.equals("verify-account")
                || targetLocation.startsWith("verify-account/")
                || targetLocation.equals("forgot-password")
                || targetLocation.startsWith("reset-password")
                || targetLocation.isEmpty();

        // If trying to access protected route without authentication
        if (!isAuthenticated && !isPublicRoute) {
            log.info("Unauthorized access attempt to: {}. Redirecting to login.", targetLocation);
            event.rerouteTo("login");
            return;
        }

        // If authenticated and trying to access login/register, redirect to home
        if (isAuthenticated && (targetLocation.equals("login") || targetLocation.equals("register"))) {
            log.info("Already authenticated. Redirecting to home.");
            event.rerouteTo("");
            return;
        }

        // Check role-based access for employees routes
        if (isAuthenticated && targetLocation.startsWith("employees")) {
            if (!securityService.hasAnyRole(RoleType.ADMIN, RoleType.MANAGER)) {
                log.warn("Access denied to {} for user without required roles", targetLocation);

                Notification notification = Notification.show(
                        "You do not have permission to access this page",
                        5000,
                        Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

                event.rerouteTo("");
            }
        }
    }
}
