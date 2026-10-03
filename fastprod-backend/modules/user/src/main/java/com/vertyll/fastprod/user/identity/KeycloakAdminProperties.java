package com.vertyll.fastprod.user.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.keycloak")
public record KeycloakAdminProperties(@NotBlank String realmUrl, @Valid @NotNull Admin admin) {
    private static final String REALMS = "/realms/";

    public String serverUrl() {
        return realmUrl.substring(0, realmUrl.indexOf(REALMS));
    }

    public String realm() {
        return realmUrl.substring(realmUrl.indexOf(REALMS) + REALMS.length());
    }

    public record Admin(@NotBlank String clientId, @NotBlank String clientSecret) {
        @Override
        public String toString() {
            return "Admin[clientId=" + clientId + ", clientSecret=***]";
        }
    }
}
