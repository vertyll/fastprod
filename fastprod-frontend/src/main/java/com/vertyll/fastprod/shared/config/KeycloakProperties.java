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
        String base = backchannelRealmUrl == null || backchannelRealmUrl.isBlank() ? realmUrl : backchannelRealmUrl;
        return base + "/protocol/openid-connect/" + path;
    }

    public String accountUrl() {
        return realmUrl + "/account";
    }
}
