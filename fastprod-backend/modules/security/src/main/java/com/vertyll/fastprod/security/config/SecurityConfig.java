package com.vertyll.fastprod.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import com.vertyll.fastprod.sharedinfrastructure.response.ApiResponse;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tools.jackson.databind.ObjectMapper;

@SuppressFBWarnings(
        value = "XSS_SERVLET",
        justification = "Only static JSON error responses are written, no user-controlled content")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String REQUIRED_TO_ACCESS_THIS_RESOURCE =
            "Authentication is required to access this resource";
    private static final String NOT_HAVE_PERMISSION_TO_ACCESS_THIS_RESOURCE =
            "You do not have permission to access this resource";

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/auth/register",
                                                "/auth/authenticate",
                                                "/auth/verify",
                                                "/auth/resend-verification-code",
                                                "/auth/refresh-token",
                                                "/auth/reset-password-request",
                                                "/auth/reset-password",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/actuator/health",
                                                "/actuator/health/**",
                                                "/api/v1/actuator/health",
                                                "/api/v1/actuator/health/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        exception ->
                                exception
                                        .authenticationEntryPoint(
                                                (_, response, _) -> {
                                                    response.setStatus(
                                                            HttpStatus.UNAUTHORIZED.value());
                                                    response.setContentType(
                                                            MediaType.APPLICATION_JSON_VALUE);

                                                    ResponseEntity<ApiResponse<Void>>
                                                            responseEntity =
                                                                    ApiResponse.buildResponse(
                                                                            null,
                                                                            REQUIRED_TO_ACCESS_THIS_RESOURCE,
                                                                            HttpStatus
                                                                                    .UNAUTHORIZED);

                                                    response.getWriter()
                                                            .write(
                                                                    objectMapper.writeValueAsString(
                                                                            responseEntity
                                                                                    .getBody()));
                                                })
                                        .accessDeniedHandler(
                                                (_, response, _) -> {
                                                    response.setStatus(
                                                            HttpStatus.FORBIDDEN.value());
                                                    response.setContentType(
                                                            MediaType.APPLICATION_JSON_VALUE);

                                                    ResponseEntity<ApiResponse<Void>>
                                                            responseEntity =
                                                                    ApiResponse.buildResponse(
                                                                            null,
                                                                            NOT_HAVE_PERMISSION_TO_ACCESS_THIS_RESOURCE,
                                                                            HttpStatus.FORBIDDEN);

                                                    response.getWriter()
                                                            .write(
                                                                    objectMapper.writeValueAsString(
                                                                            responseEntity
                                                                                    .getBody()));
                                                }));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
