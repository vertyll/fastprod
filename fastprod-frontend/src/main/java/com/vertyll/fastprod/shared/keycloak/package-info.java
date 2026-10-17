/**
 * The person's account on Keycloak: the password and the second factor, changed on Keycloak's pages, which
 * {@code shared.security} opens, and the language the reader shares with those pages. The rest of the application
 * reaches it through {@link KeycloakAccounts} and {@link KeycloakPreferences}.
 */
@NullMarked
package com.vertyll.fastprod.shared.keycloak;

import org.jspecify.annotations.NullMarked;
