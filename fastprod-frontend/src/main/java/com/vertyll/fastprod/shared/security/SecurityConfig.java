package com.vertyll.fastprod.shared.security;

import java.time.Clock;
import java.util.Map;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.DelegatingOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

import com.vertyll.fastprod.shared.config.KeycloakProperties;

import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
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
        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeTokens,
        JwtDecoder accessTokens,
        KeycloakProperties keycloak,
        VaadinDefaultRequestCache requestCache
    ) {
        // Spring Security's saved request, which a sign-in started from a button also returns through, see ReturnPage.
        requestCache.setDelegateRequestCache(ReturnPage.requestCache());
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/actuator/health/**").permitAll());
        // Written only where the response has no policy yet, so Vaadin's page keeps the one with its nonce.
        http.headers(
            headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(ContentSecurityPolicy.forOtherResponses()))
        );
        http.oauth2Login(
            login -> login
                .authorizationEndpoint(
                    endpoint -> endpoint
                        .authorizationRequestResolver(new LocalizedAuthorizationRequestResolver(clientRegistrations))
                )
                .tokenEndpoint(token -> token.accessTokenResponseClient(authorizationCodeTokens))
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
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeTokenResponseClient(
        RestClient.Builder restClients
    ) {
        RestClientAuthorizationCodeTokenResponseClient client = new RestClientAuthorizationCodeTokenResponseClient();
        client.setRestClient(tokenEndpointClient(restClients));
        return client;
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenResponseClient(
        RestClient.Builder restClients
    ) {
        RestClientRefreshTokenTokenResponseClient client = new RestClientRefreshTokenTokenResponseClient();
        client.setRestClient(tokenEndpointClient(restClients));
        return client;
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrations,
        OAuth2AuthorizedClientRepository authorizedClients,
        OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokens
    ) {
        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        manager.setAuthorizedClientProvider(
            new DelegatingOAuth2AuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().authorizationCode().build(),
                new SingleFlightRefreshTokenProvider(
                    OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken(refresh -> refresh.accessTokenResponseClient(refreshTokens))
                        .build(),
                    Clock.systemUTC()
                )
            )
        );
        return manager;
    }

    @Bean
    JwtDecoder accessTokenDecoder(KeycloakProperties keycloak, RestTemplateBuilder restTemplates) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(keycloak.backchannelEndpoint("certs"))
            .restOperations(restTemplates.build())
            .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(keycloak.realmUrl()));
        return decoder;
    }

    /**
     * The client Spring Security's token response clients use by default, on Boot's builder, so the token endpoint is
     * called with the timeouts of {@code spring.http.clients}.
     */
    private static RestClient tokenEndpointClient(RestClient.Builder restClients) {
        return restClients.configureMessageConverters(converters -> {
            converters.addCustomConverter(new FormHttpMessageConverter());
            converters.addCustomConverter(new OAuth2AccessTokenResponseHttpMessageConverter());
        }).defaultStatusHandler(new OAuth2ErrorResponseErrorHandler()).build();
    }
}
