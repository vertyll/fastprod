package com.vertyll.fastprod.shared.keycloak;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.vertyll.fastprod.shared.security.AccountActionPages;
import com.vertyll.fastprod.shared.security.AccountActionPages.AccountAction;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class KeycloakAccountsTest {

    private final KeycloakAccount account = mock(KeycloakAccount.class);
    private final AccountActionPages pages = mock(AccountActionPages.class);
    private final KeycloakAccounts accounts = new KeycloakAccounts(account, pages);

    @Test
    void changingThePasswordStoresTheLanguageBeforeKeycloakShowsItsPage() {
        accounts.changePassword("en");

        InOrder order = inOrder(account, pages);
        order.verify(account).keepLanguage("en");
        order.verify(pages).start(AccountAction.UPDATE_PASSWORD);
    }

    @Test
    void turningTheSecondFactorOnStoresTheLanguageBeforeKeycloakShowsItsPage() {
        accounts.turnOnSecondFactor("en");

        InOrder order = inOrder(account, pages);
        order.verify(account).keepLanguage("en");
        order.verify(pages).start(AccountAction.CONFIGURE_TOTP);
    }

    @Test
    void turningTheSecondFactorOffStoresTheLanguageBeforeKeycloakAsksToRemoveIt() {
        accounts.turnOffSecondFactor("0b7f6a52-3c1e-4d2a-9f5b-6e8d7c1a2b3c", "en");

        InOrder order = inOrder(account, pages);
        order.verify(account).keepLanguage("en");
        order.verify(pages).removeCredential("0b7f6a52-3c1e-4d2a-9f5b-6e8d7c1a2b3c");
    }
}
