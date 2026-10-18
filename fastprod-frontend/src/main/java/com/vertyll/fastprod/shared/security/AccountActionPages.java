package com.vertyll.fastprod.shared.security;

import java.io.Serial;
import java.io.Serializable;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.SpringComponent;

/**
 * Keycloak's pages a signed-in person is sent to for one account action, each returning to the application when done,
 * so no password and no TOTP secret passes through it. Only the Keycloak module sends a person here, after storing the
 * application's language on the account.
 */
@SpringComponent
public class AccountActionPages implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public void start(AccountAction action) {
        open(action.name());
    }

    /** Keycloak's page removing the credential with this id, after the person confirms. */
    public void removeCredential(String credentialId) {
        open(LocalizedAuthorizationRequestResolver.DELETE_CREDENTIAL + credentialId);
    }

    private static void open(String action) {
        UI ui = UI.getCurrent();
        ui.getPage()
            .setLocation(
                SecurityConfig.LOGIN_PATH + "?" + LocalizedAuthorizationRequestResolver.ACCOUNT_ACTION_PARAMETER + "="
                        + action + "&" + KeycloakPages.returnTo(ui)
            );
    }

    /** The Keycloak pages a signed-in person may be sent to, by the names Keycloak gives them in {@code kc_action}. */
    public enum AccountAction {
        UPDATE_PASSWORD,
        CONFIGURE_TOTP
    }
}
