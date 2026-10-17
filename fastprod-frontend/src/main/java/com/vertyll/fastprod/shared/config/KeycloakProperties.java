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

    public String endpoint(String path) {
        return realmUrl + "/protocol/openid-connect/" + path;
    }

    public String backchannelEndpoint(String path) {
        return backchannelRealm() + "/protocol/openid-connect/" + path;
    }

    /** A path of Keycloak's account API, which answers about the person whose access token calls it. */
    public String backchannelAccountEndpoint(String path) {
        return backchannelRealm() + "/account/" + path;
    }

    private String backchannelRealm() {
        return backchannelRealmUrl == null || backchannelRealmUrl.isBlank() ? realmUrl : backchannelRealmUrl;
    }
}
