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

    /**
     * Sends a signed-in person to one Keycloak page, which returns to the application when done, so no password and no
     * TOTP secret passes through it.
     */
    public static void start(AccountAction action) {
        UI.getCurrent()
            .getPage()
            .setLocation(
                SecurityConfig.LOGIN_PATH + "?" + LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER + "="
                        + action.name()
            );
    }

    /** Sends a signed-in person to Keycloak's page removing the credential with this id, after they confirm. */
    public static void removeCredential(String credentialId) {
        UI.getCurrent()
            .getPage()
            .setLocation(
                SecurityConfig.LOGIN_PATH + "?" + LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER + "="
                        + LocalizedAuthorizationRequestResolver.DELETE_CREDENTIAL + credentialId
            );
    }

    /** The Keycloak pages a signed-in person may be sent to, by the names Keycloak gives them in {@code kc_action}. */
    public enum AccountAction {
        UPDATE_PASSWORD,
        CONFIGURE_TOTP
    }
}
