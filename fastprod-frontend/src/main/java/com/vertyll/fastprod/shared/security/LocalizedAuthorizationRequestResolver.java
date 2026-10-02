package com.vertyll.fastprod.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.vertyll.fastprod.base.ui.component.LanguageSwitcher;

final class LocalizedAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    static final String REGISTER_PARAMETER = "register";

    private static final String DEFAULT_LANGUAGE = "pl";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    LocalizedAuthorizationRequestResolver(ClientRegistrationRepository registrations) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
            registrations,
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
        this.delegate.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
    }

    @Override
    public @Nullable OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return localized(request, delegate.resolve(request));
    }

    @Override
    public @Nullable OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return localized(request, delegate.resolve(request, clientRegistrationId));
    }

    private static @Nullable OAuth2AuthorizationRequest localized(
        HttpServletRequest request,
        @Nullable OAuth2AuthorizationRequest authorization
    ) {
        if (authorization == null) {
            return null;
        }
        boolean register = request.getParameter(REGISTER_PARAMETER) != null;
        String language = language(request);
        return OAuth2AuthorizationRequest.from(authorization).additionalParameters(parameters -> {
            parameters.put("ui_locales", language);
            if (register) {
                parameters.put("prompt", "create");
            }
        }).build();
    }

    private static String language(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object language = session == null ? null : session.getAttribute(LanguageSwitcher.LANGUAGE_ATTRIBUTE);
        return "en".equals(language) ? "en" : DEFAULT_LANGUAGE;
    }
}
