package com.vertyll.fastprod.shared.security;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenProvider {

    private static final String BEARER = "Bearer ";

    private final OAuth2AuthorizedClientManager authorizedClients;

    public Optional<String> getAuthorizationHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken token)
                || !(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Optional.empty();
        }
        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            return Optional.empty();
        }
        try {
            OAuth2AuthorizedClient client = authorizedClients.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(token.getAuthorizedClientRegistrationId())
                    .principal(token)
                    .attribute(HttpServletRequest.class.getName(), attributes.getRequest())
                    .attribute(HttpServletResponse.class.getName(), response)
                    .build()
            );
            return Optional.ofNullable(client).map(authorized -> BEARER + authorized.getAccessToken().getTokenValue());
        } catch (ClientAuthorizationException e) {
            log.info("Keycloak session ended: {}", e.getError().getErrorCode());
            return Optional.empty();
        }
    }
}
