package com.vertyll.fastprod.shared.security;

import com.vaadin.flow.component.UI;

public final class KeycloakPages {

    private KeycloakPages() {
    }

    public static void signIn() {
        UI.getCurrent().getPage().setLocation(SecurityConfig.LOGIN_PATH);
    }

    public static void signUp() {
        UI.getCurrent()
            .getPage()
            .setLocation(SecurityConfig.LOGIN_PATH + "?" + LocalizedAuthorizationRequestResolver.REGISTER_PARAMETER);
    }
}
