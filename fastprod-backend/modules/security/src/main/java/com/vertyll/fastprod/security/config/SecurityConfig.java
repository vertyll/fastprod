package com.vertyll.fastprod.security.config;

import java.util.Collection;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(KeycloakResourceServerProperties.class)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        ProblemSecurityHandlers problems = new ProblemSecurityHandlers(objectMapper);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                auth -> auth
                    .requestMatchers(
                        "/translations/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/health",
                        "/actuator/health/**"
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(
                resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakTokens()))
                    .authenticationEntryPoint(problems)
                    .accessDeniedHandler(problems)
            )
            .exceptionHandling(exception -> exception.authenticationEntryPoint(problems).accessDeniedHandler(problems));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(KeycloakResourceServerProperties keycloak, RestTemplateBuilder restTemplates) {
        // Boot's builder calls the key set with the timeouts of spring.http.clients.
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withJwkSetUri(keycloak.jwkSetUri()).restOperations(restTemplates.build()).build();
        decoder.setJwtValidator(tokenValidator(keycloak));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> tokenValidator(KeycloakResourceServerProperties keycloak) {
        return new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(keycloak.realmUrl()),
            new JwtClaimValidator<Collection<String>>(
                JwtClaimNames.AUD,
                audience -> audience != null && audience.contains(keycloak.audience())
            )
        );
    }

    private static JwtAuthenticationConverter keycloakTokens() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoles());
        converter.setPrincipalClaimName(StandardClaimNames.EMAIL);
        return converter;
    }
}
