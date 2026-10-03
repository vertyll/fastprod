package com.vertyll.fastprod.shared.security;

import java.time.Clock;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.DelegatingOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.vertyll.fastprod.shared.config.KeycloakProperties;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class SecurityConfig {

    static final String REGISTRATION_ID = "keycloak";
    static final String LOGIN_PATH = "/oauth2/authorization/" + REGISTRATION_ID;

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        ClientRegistrationRepository clientRegistrations,
        OAuth2AuthorizedClientRepository authorizedClients,
        JwtDecoder accessTokens,
        KeycloakProperties keycloak
    ) {
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/actuator/health/**").permitAll());
        http.oauth2Login(
            login -> login
                .authorizationEndpoint(
                    endpoint -> endpoint
                        .authorizationRequestResolver(new LocalizedAuthorizationRequestResolver(clientRegistrations))
                )
                .authorizedClientRepository(authorizedClients)
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new KeycloakOidcUserService(accessTokens)))
        );
        http.with(
            VaadinSecurityConfigurer.vaadin(),
            vaadin -> vaadin.oauth2LoginPage(LOGIN_PATH, keycloak.appUrl() + "/")
        );
        return http.build();
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(KeycloakProperties keycloak) {
        ClientRegistration registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientName("Keycloak")
            .clientId(keycloak.clientId())
            .clientSecret(keycloak.clientSecret())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(keycloak.appUrl() + "/login/oauth2/code/{registrationId}")
            .scope(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL)
            .authorizationUri(keycloak.endpoint("auth"))
            .tokenUri(keycloak.backchannelEndpoint("token"))
            .jwkSetUri(keycloak.backchannelEndpoint("certs"))
            .issuerUri(keycloak.realmUrl())
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .providerConfigurationMetadata(Map.of("end_session_endpoint", keycloak.endpoint("logout")))
            .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrations,
        OAuth2AuthorizedClientRepository authorizedClients
    ) {
        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        manager.setAuthorizedClientProvider(
            new DelegatingOAuth2AuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().authorizationCode().build(),
                new SingleFlightRefreshTokenProvider(
                    OAuth2AuthorizedClientProviderBuilder.builder().refreshToken().build(),
                    Clock.systemUTC()
                )
            )
        );
        return manager;
    }

    @Bean
    JwtDecoder accessTokenDecoder(KeycloakProperties keycloak) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(keycloak.backchannelEndpoint("certs")).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(keycloak.realmUrl()));
        return decoder;
    }
}
