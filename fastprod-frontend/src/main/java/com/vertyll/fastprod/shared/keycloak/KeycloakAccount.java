package com.vertyll.fastprod.shared.keycloak;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vertyll.fastprod.shared.config.KeycloakProperties;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Keycloak's account API, called with the signed-in person's own access token: it answers about that person alone, so
 * the application needs no right to read anyone else's credentials.
 */
@Slf4j
@Component
class KeycloakAccount {

    private static final String OTP_CREDENTIAL = "otp";

    private static final String LOCALE_ATTRIBUTE = "locale";

    private static final String AUTHORIZATION = "Authorization";

    private static final String JSON = "application/json";

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String accountUrl;
    private final String credentialsUrl;
    private final AuthTokenProvider tokens;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    @Autowired
    KeycloakAccount(KeycloakProperties keycloak, AuthTokenProvider tokens, ObjectMapper objectMapper) {
        this(keycloak.backchannelRealm() + "/account/", tokens, objectMapper);
    }

    KeycloakAccount(String accountUrl, AuthTokenProvider tokens, ObjectMapper objectMapper) {
        this.accountUrl = accountUrl;
        this.credentialsUrl = accountUrl + "credentials?type=" + OTP_CREDENTIAL;
        this.tokens = tokens;
        this.objectMapper = objectMapper;
    }

    /**
     * The signed-in person's second factor, or empty when Keycloak could not be asked, so the profile offers neither
     * turning it on nor off.
     */
    Optional<SecondFactor> secondFactor() {
        return tokens.getAuthorizationHeader().flatMap(this::secondFactor);
    }

    Optional<SecondFactor> secondFactor(String authorization) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(credentialsUrl))
            .timeout(TIMEOUT)
            .header(AUTHORIZATION, authorization)
            .header("Accept", JSON)
            .GET()
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Keycloak's account API answered {}", response.statusCode());
                return Optional.empty();
            }
            CredentialType[] types = objectMapper.readValue(response.body(), CredentialType[].class);
            return Optional.of(
                new SecondFactor(
                    Arrays.stream(types)
                        .filter(type -> OTP_CREDENTIAL.equals(type.type()))
                        .flatMap(type -> type.credentials().stream())
                        .map(metadata -> metadata.credential().id())
                        .findFirst()
                        .orElse(null)
                )
            );
        } catch (IOException | JacksonException e) {
            log.warn("Could not read the second factor from Keycloak: {}", e.getMessage());
            return Optional.empty();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /**
     * Stores the language on the signed-in person's account, which Keycloak's pages show a signed-in person in, over
     * the language the application asks for. The account is read and written back whole, so nothing else on it changes;
     * a failure leaves Keycloak's pages in the language they had, and is only logged.
     */
    void keepLanguage(String language) {
        tokens.getAuthorizationHeader().ifPresent(authorization -> keepLanguage(authorization, language));
    }

    void keepLanguage(String authorization, String language) {
        try {
            HttpResponse<String> read = httpClient.send(
                HttpRequest.newBuilder(URI.create(accountUrl))
                    .timeout(TIMEOUT)
                    .header(AUTHORIZATION, authorization)
                    .header("Accept", JSON)
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );
            if (read.statusCode() != 200 || !(objectMapper.readTree(read.body()) instanceof ObjectNode account)) {
                log.warn("Keycloak's account API answered {}", read.statusCode());
                return;
            }
            ObjectNode attributes =
                    account.get("attributes") instanceof ObjectNode stored ? stored : account.putObject("attributes");
            JsonNode stored = attributes.get(LOCALE_ATTRIBUTE);
            if (stored != null && stored.size() == 1 && language.equals(stored.get(0).asString())) {
                return;
            }
            attributes.putArray(LOCALE_ATTRIBUTE).add(language);
            HttpResponse<String> written = httpClient.send(
                HttpRequest.newBuilder(URI.create(accountUrl))
                    .timeout(TIMEOUT)
                    .header(AUTHORIZATION, authorization)
                    .header("Content-Type", JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(account)))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );
            if (written.statusCode() >= 300) {
                log.warn("Keycloak did not store the account's language: {}", written.statusCode());
            }
        } catch (IOException | JacksonException e) {
            log.warn("Could not store the account's language in Keycloak: {}", e.getMessage());
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CredentialType(String type, @Nullable List<CredentialMetadata> userCredentialMetadatas) {

        List<CredentialMetadata> credentials() {
            return userCredentialMetadatas == null ? List.of() : userCredentialMetadatas;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CredentialMetadata(Credential credential) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Credential(String id) {
    }
}
