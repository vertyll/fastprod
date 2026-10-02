package com.vertyll.fastprod.shared.security;

import java.util.Objects;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;

final class KeycloakOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final JwtDecoder accessTokens;

    KeycloakOidcUserService(JwtDecoder accessTokens) {
        this.accessTokens = accessTokens;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser user = Objects.requireNonNull(delegate.loadUser(request), "Keycloak returned no user");
        return new DefaultOidcUser(
            KeycloakRoles.of(accessTokens.decode(request.getAccessToken().getTokenValue())),
            user.getIdToken(),
            user.getUserInfo(),
            StandardClaimNames.EMAIL
        );
    }
}
