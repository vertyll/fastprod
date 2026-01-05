package com.vertyll.fastprod.shared.security;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringComponent
@Slf4j
@RequiredArgsConstructor
public class TokenRefreshListener implements VaadinServiceInitListener {

    private final transient TokenRefreshService tokenRefreshService;
    private final transient SecurityService securityService;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource()
                .addUIInitListener(
                        uiEvent ->
                                uiEvent.getUI()
                                        .addBeforeEnterListener(
                                                enterEvent -> {
                                                    String location =
                                                            enterEvent.getLocation().getPath();
                                                    if (isPublicRoute(location)) {
                                                        return;
                                                    }

                                                    if (securityService.isAuthenticated()) {
                                                        boolean tokenValid =
                                                                tokenRefreshService
                                                                        .ensureValidToken();

                                                        if (!tokenValid) {
                                                            log.warn(
                                                                    "Token refresh failed, redirecting to login");
                                                            securityService.logout();
                                                            tokenRefreshService
                                                                    .clearTokenExpiration();
                                                            enterEvent.forwardTo("login");
                                                        }
                                                    }
                                                }));
    }

    private boolean isPublicRoute(String route) {
        return route.equals("login")
                || route.equals("register")
                || route.startsWith("verify-account")
                || route.isEmpty();
    }
}
