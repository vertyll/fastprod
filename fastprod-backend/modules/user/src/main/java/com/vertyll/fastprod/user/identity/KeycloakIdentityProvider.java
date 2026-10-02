package com.vertyll.fastprod.user.identity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class KeycloakIdentityProvider implements IdentityProvider {

    private static final String EMAIL_TAKEN = "errors.user.emailTaken";
    private static final String IDENTITY_UNAVAILABLE = "errors.identity.unavailable";
    private static final String VERIFY_EMAIL = "VERIFY_EMAIL";

    private final Keycloak keycloak;
    private final KeycloakAdminProperties properties;

    @Override
    public String createUser(NewIdentity identity, String temporaryPassword, Set<RoleType> roles) {
        UserRepresentation user = representation(identity);
        user.setEnabled(true);
        user.setCredentials(List.of(temporaryPassword(temporaryPassword)));
        try (Response response = realm().users().create(user)) {
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                throw new ApiException(EMAIL_TAKEN, HttpStatus.BAD_REQUEST);
            }
            String keycloakId = CreatedResponseUtil.getCreatedId(response);
            replaceRoles(keycloakId, roles);
            return keycloakId;
        } catch (WebApplicationException e) {
            throw unavailable(e);
        }
    }

    @Override
    public void updateUser(String keycloakId, NewIdentity identity) {
        UserResource resource = realm().users().get(keycloakId);
        UserRepresentation current = call(resource::toRepresentation);
        UserRepresentation update = representation(identity);
        if (identity.email().equals(current.getEmail())) {
            update.setEmailVerified(current.isEmailVerified());
            update.setRequiredActions(current.getRequiredActions());
        }
        call(() -> {
            resource.update(update);
            return null;
        });
    }

    @Override
    public void resetPassword(String keycloakId, String temporaryPassword) {
        call(() -> {
            realm().users().get(keycloakId).resetPassword(temporaryPassword(temporaryPassword));
            return null;
        });
    }

    @Override
    public void replaceRoles(String keycloakId, Set<RoleType> roles) {
        call(() -> {
            RoleScopeResource mappings = realm().users().get(keycloakId).roles().realmLevel();
            List<RoleRepresentation> managed = Arrays.stream(RoleType.values())
                .map(role -> realm().roles().get(role.getValue()).toRepresentation())
                .toList();
            List<RoleRepresentation> granted =
                    managed.stream().filter(role -> roles.contains(RoleType.fromValue(role.getName()))).toList();
            mappings.remove(managed.stream().filter(role -> !granted.contains(role)).toList());
            mappings.add(granted);
            return null;
        });
    }

    @Override
    public void rename(String keycloakId, String firstName, String lastName) {
        UserResource resource = realm().users().get(keycloakId);
        call(() -> {
            UserRepresentation user = resource.toRepresentation();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            resource.update(user);
            return null;
        });
    }

    @Override
    public void disable(String keycloakId) {
        UserResource resource = realm().users().get(keycloakId);
        call(() -> {
            UserRepresentation user = resource.toRepresentation();
            user.setEnabled(false);
            resource.update(user);
            resource.logout();
            return null;
        });
    }

    private RealmResource realm() {
        return keycloak.realm(properties.realm());
    }

    private static UserRepresentation representation(NewIdentity identity) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(identity.email());
        user.setEmail(identity.email());
        user.setFirstName(identity.firstName());
        user.setLastName(identity.lastName());
        user.setEmailVerified(false);
        user.setRequiredActions(List.of(VERIFY_EMAIL));
        return user;
    }

    private static CredentialRepresentation temporaryPassword(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(true);
        return credential;
    }

    private static <T> T call(KeycloakCall<T> call) {
        try {
            return call.run();
        } catch (WebApplicationException e) {
            throw unavailable(e);
        }
    }

    private static ApiException unavailable(WebApplicationException e) {
        return new ApiException(IDENTITY_UNAVAILABLE, HttpStatus.BAD_GATEWAY, e);
    }

    @FunctionalInterface
    private interface KeycloakCall<T> {
        T run();
    }
}
