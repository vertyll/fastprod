package com.vertyll.fastprod.security.config;

import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("security.keycloak")
public record KeycloakResourceServerProperties(@NotBlank String realmUrl, @Nullable String backchannelRealmUrl) {

    String jwkSetUri() {
        String base = backchannelRealmUrl == null || backchannelRealmUrl.isBlank() ? realmUrl : backchannelRealmUrl;
        return base + "/protocol/openid-connect/certs";
    }
}
