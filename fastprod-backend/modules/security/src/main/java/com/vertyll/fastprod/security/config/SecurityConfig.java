package com.vertyll.fastprod.security.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String REQUIRED_TO_ACCESS_THIS_RESOURCE = "errors.auth.authenticationRequired";
    private static final String NOT_HAVE_PERMISSION_TO_ACCESS_THIS_RESOURCE = "errors.auth.forbidden";

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                auth -> auth
                    .requestMatchers(
                        "/auth/register",
                        "/auth/authenticate",
                        "/auth/verify",
                        "/auth/resend-verification-code",
                        "/auth/refresh-token",
                        "/auth/reset-password-request",
                        "/auth/reset-password",
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
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
