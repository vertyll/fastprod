package com.vertyll.fastprod.user.identity;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.spi.ResteasyClientClassicProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeycloakAdminProperties.class)
class KeycloakAdminConfig {

    /** The Jackson provider's priority in the client Keycloak builds for itself when given none. */
    private static final int JACKSON_PRIORITY = 100;

    @Bean(destroyMethod = "close")
    Keycloak keycloakAdmin(KeycloakAdminProperties properties, HttpClientSettings httpClients) {
        return KeycloakBuilder.builder()
            .serverUrl(properties.serverUrl())
            .realm(properties.realm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(properties.admin().clientId())
            .clientSecret(properties.admin().clientSecret())
            .resteasyClient(timed(ResteasyClientClassicProvider.createClientBuilder(), httpClients).build())
            .build();
    }

    /**
     * The client Keycloak would build for itself, with the timeouts of {@code spring.http.clients}, so a Keycloak
     * that stops answering fails the call instead of holding its thread.
     */
    private static ResteasyClientBuilderImpl timed(ResteasyClientBuilderImpl client, HttpClientSettings httpClients) {
        client.register(JacksonProvider.class, JACKSON_PRIORITY);
        Duration connectTimeout = httpClients.connectTimeout();
        if (connectTimeout != null) {
            client.connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        Duration readTimeout = httpClients.readTimeout();
        if (readTimeout != null) {
            client.readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        return client;
    }
}
