package com.vertyll.fastprod.shared.keycloak;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

import com.vertyll.fastprod.shared.security.KeycloakPages;
import com.vertyll.fastprod.shared.security.KeycloakPages.AccountAction;

import com.vaadin.flow.spring.annotation.SpringComponent;

/**
 * The signed-in person's account on Keycloak: the password and the second factor, each changed on Keycloak's own pages.
 * Keycloak shows a signed-in person their account's language, so each action first stores the language the application
 * shows there.
 */
@SpringComponent
public class KeycloakAccounts implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient KeycloakAccount account;

    KeycloakAccounts(KeycloakAccount account) {
        this.account = account;
    }

    /** The person's second factor, or empty when Keycloak could not be asked. */
    public Optional<SecondFactor> secondFactor() {
        return account.secondFactor();
    }

    public void changePassword(String language) {
        account.keepLanguage(language);
        KeycloakPages.start(AccountAction.UPDATE_PASSWORD);
    }

    public void turnOnSecondFactor(String language) {
        account.keepLanguage(language);
        KeycloakPages.start(AccountAction.CONFIGURE_TOTP);
    }

    /** Opens Keycloak's page removing the second factor, which asks the person to confirm. */
    public void turnOffSecondFactor(String credentialId, String language) {
        account.keepLanguage(language);
        KeycloakPages.removeCredential(credentialId);
    }
}
