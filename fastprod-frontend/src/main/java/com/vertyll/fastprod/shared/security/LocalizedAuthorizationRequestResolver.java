package com.vertyll.fastprod.shared.security;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.vertyll.fastprod.shared.security.AccountActionPages.AccountAction;

final class LocalizedAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    static final String REGISTER_PARAMETER = "register";

    static final String ACCOUNT_ACTION_PARAMETER = "kc_action";

    /** Keycloak's action removing one credential, named by its id: {@code delete_credential:<id>}. */
    static final String DELETE_CREDENTIAL = "delete_credential:";

    private static final Pattern CREDENTIAL_ID =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

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
        Optional<String> action = accountAction(request);
        return OAuth2AuthorizationRequest.from(authorization).additionalParameters(parameters -> {
            parameters.put("ui_locales", language);
            if (register) {
                parameters.put("prompt", "create");
            }
            action.ifPresent(allowed -> parameters.put(ACCOUNT_ACTION_PARAMETER, allowed));
        }).build();
    }

    /**
     * The action Keycloak may run for a signed-in person: one of {@link AccountAction}, or removing a credential named
     * by its UUID, which Keycloak asks to confirm and removes only when it is the person's own.
     */
    private static Optional<String> accountAction(HttpServletRequest request) {
        String requested = request.getParameter(ACCOUNT_ACTION_PARAMETER);
        if (requested == null) {
            return Optional.empty();
        }
        if (requested.startsWith(DELETE_CREDENTIAL)) {
            return CREDENTIAL_ID.matcher(requested.substring(DELETE_CREDENTIAL.length())).matches()
                    ? Optional.of(requested) : Optional.empty();
        }
        return Arrays.stream(AccountAction.values()).map(AccountAction::name).filter(requested::equals).findFirst();
    }

    private static String language(HttpServletRequest request) {
        return SessionLanguage.of(request.getSession(false)).orElse(DEFAULT_LANGUAGE);
    }
}
