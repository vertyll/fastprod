package com.vertyll.fastprod.shared.keycloak;

import org.jspecify.annotations.Nullable;

/** The one-time-password credential, by the id Keycloak's {@code delete_credential} action takes; null when none. */
public record SecondFactor(@Nullable String credentialId) {
}
