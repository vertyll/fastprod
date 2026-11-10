package com.vertyll.fastprod.security.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemSecurityHandlersTest {

    // As Spring's own converters render a problem, with its properties at the top level.
    private final ObjectMapper objectMapper =
            JsonMapper.builder().addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class).build();
    private final ProblemSecurityHandlers handlers = new ProblemSecurityHandlers(objectMapper);
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void aRequestWithoutATokenIsChallengedForOne() throws IOException {
        handlers.commence(
            new MockHttpServletRequest(),
            response,
            new InsufficientAuthenticationException("Full authentication is required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer ").doesNotContain("error=");
        assertProblem(401, ProblemSecurityHandlers.AUTHENTICATION_REQUIRED);
    }

    @Test
    void aRefusedTokenIsNamedInvalidInTheChallenge() throws IOException {
        handlers.commence(new MockHttpServletRequest(), response, new InvalidBearerTokenException("Jwt expired"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer error=\"invalid_token\"");
        assertProblem(401, ProblemSecurityHandlers.AUTHENTICATION_REQUIRED);
    }

    @Test
    void aValidTokenWithoutThePermissionIsNamedInsufficientInTheChallenge() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setUserPrincipal(new JwtAuthenticationToken(token()));

        handlers.handle(request, response, new AccessDeniedException("Access Denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer error=\"insufficient_scope\"");
        assertProblem(403, ProblemSecurityHandlers.FORBIDDEN);
    }

    private void assertProblem(int status, String code) throws UnsupportedEncodingException {
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        JsonNode problem = objectMapper.readTree(response.getContentAsString());
        assertThat(problem.get("status").asInt()).isEqualTo(status);
        assertThat(problem.get("code").asString()).isEqualTo(code);
    }

    private static Jwt token() {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("user")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    }
}
