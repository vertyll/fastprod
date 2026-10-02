package com.vertyll.fastprod.security.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(KeycloakResourceServerProperties.class)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String REQUIRED_TO_ACCESS_THIS_RESOURCE = "errors.auth.authenticationRequired";
    private static final String NOT_HAVE_PERMISSION_TO_ACCESS_THIS_RESOURCE = "errors.auth.forbidden";

    private final ObjectMapper objectMapper;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
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
                        "/actuator/health/**",
                        "/api/v1/actuator/health",
                        "/api/v1/actuator/health/**"
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(
                resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakTokens()))
            )
            .exceptionHandling(
                exception -> exception
                    .authenticationEntryPoint(
                        (
                            _,
                            response,
                            _
                        ) -> writeProblem(response, HttpStatus.UNAUTHORIZED, REQUIRED_TO_ACCESS_THIS_RESOURCE)
                    )
                    .accessDeniedHandler(
                        (
                            _,
                            response,
                            _
                        ) -> writeProblem(response, HttpStatus.FORBIDDEN, NOT_HAVE_PERMISSION_TO_ACCESS_THIS_RESOURCE)
                    )
            );

        return http.build();
    }

    @SuppressFBWarnings(
        value = "XSS_SERVLET",
        justification = "Only static JSON error responses are written, no user-controlled content"
    )
    private void writeProblem(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(GlobalExceptionHandler.problem(status, detail)));
    }

    @Bean
    public JwtDecoder jwtDecoder(KeycloakResourceServerProperties keycloak) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(keycloak.jwkSetUri()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(keycloak.realmUrl()));
        return decoder;
    }

    private static JwtAuthenticationConverter keycloakTokens() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoles());
        converter.setPrincipalClaimName(StandardClaimNames.EMAIL);
        return converter;
    }
}
