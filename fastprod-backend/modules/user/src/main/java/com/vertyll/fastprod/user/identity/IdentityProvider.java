package com.vertyll.fastprod.user.identity;

import java.util.Set;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

public interface IdentityProvider {

    String createUser(NewIdentity identity, String temporaryPassword, Set<RoleType> roles);

    void updateUser(String keycloakId, NewIdentity identity);

    void resetPassword(String keycloakId, String temporaryPassword);

    void replaceRoles(String keycloakId, Set<RoleType> roles);

    void rename(String keycloakId, String firstName, String lastName);

    void disable(String keycloakId);

    record NewIdentity(String email, String firstName, String lastName) {
    }
}
