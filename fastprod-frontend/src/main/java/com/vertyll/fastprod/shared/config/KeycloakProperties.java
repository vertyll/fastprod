package com.vertyll.fastprod.shared.config;

import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.keycloak")
public record KeycloakProperties(
    @NotBlank String realmUrl,
    @Nullable String backchannelRealmUrl,
    @NotBlank String clientId,
    @NotBlank String clientSecret,
    @NotBlank String appUrl
) {

    /** The realm's name, the last segment of {@code realmUrl}. */
    public String realm() {
        return realmUrl.substring(realmUrl.lastIndexOf('/') + 1);
    }

    public String endpoint(String path) {
        return realmUrl + "/protocol/openid-connect/" + path;
    }

    public String backchannelEndpoint(String path) {
        return backchannelRealm() + "/protocol/openid-connect/" + path;
    }

    /** The realm as the application reaches it from inside the cluster, which may differ from the browser's address. */
    public String backchannelRealm() {
        return backchannelRealmUrl == null || backchannelRealmUrl.isBlank() ? realmUrl : backchannelRealmUrl;
    }
}
