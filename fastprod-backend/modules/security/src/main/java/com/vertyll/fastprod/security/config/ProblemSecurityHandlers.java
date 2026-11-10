package com.vertyll.fastprod.security.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.vertyll.fastprod.sharedinfrastructure.exception.GlobalExceptionHandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tools.jackson.databind.ObjectMapper;

/**
 * Answers the refusals of the security filter chain, which no controller advice sees, with the problem document the
 * rest of the API answers with, and with the challenge RFC 6750 asks for: {@code WWW-Authenticate: Bearer}, with
 * {@code error="invalid_token"} when a token was sent and refused, and {@code error="insufficient_scope"} when a valid
 * token lacks the permission.
 */
final class ProblemSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    static final String AUTHENTICATION_REQUIRED = "errors.auth.authenticationRequired";
    static final String FORBIDDEN = "errors.auth.forbidden";

    private final BearerTokenAuthenticationEntryPoint bearerChallenge = new BearerTokenAuthenticationEntryPoint();
    private final BearerTokenAccessDeniedHandler insufficientScope = new BearerTokenAccessDeniedHandler();
    private final ObjectMapper objectMapper;

    ProblemSecurityHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        // Sets the status and the challenge, and leaves the body to us.
        bearerChallenge.commence(request, response, exception);
        writeProblem(response, AUTHENTICATION_REQUIRED);
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception
    ) throws IOException {
        insufficientScope.handle(request, response, exception);
        writeProblem(response, FORBIDDEN);
    }

    @SuppressFBWarnings(
        value = "XSS_SERVLET",
        justification = "Only static JSON error responses are written, no user-controlled content"
    )
    private void writeProblem(HttpServletResponse response, String messageKey) throws IOException {
        HttpStatus status = HttpStatus.valueOf(response.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(GlobalExceptionHandler.problem(status, messageKey)));
    }
}
