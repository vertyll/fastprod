package com.vertyll.fastprod.shared.keycloak;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.vertyll.fastprod.shared.security.AuthTokenProvider;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakAccountTest {

    private static final String ACCOUNT = "/realms/fastprod/account/";

    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicReference<String> body = new AtomicReference<>("");
    private final List<String> written = new ArrayList<>();

    @Test
    void theSecondFactorIsReadWithTheCallersOwnToken() throws IOException {
        body.set("""
                [{"type":"otp","category":"two-factor",
                  "userCredentialMetadatas":[{"credential":{"id":"otp-1","type":"otp","userLabel":"phone"}}]}]
                """);

        assertThat(withKeycloak(account -> account.secondFactor("Bearer access-1")))
            .contains(new SecondFactor("otp-1"));
        assertThat(authorization.get()).isEqualTo("Bearer access-1");
    }

    @Test
    void noOneTimePasswordIsASecondFactorTurnedOff() throws IOException {
        body.set("[{\"type\":\"otp\",\"category\":\"two-factor\",\"userCredentialMetadatas\":[]}]");

        assertThat(withKeycloak(account -> account.secondFactor("Bearer access-1"))).contains(new SecondFactor(null));
    }

    @Test
    void aRefusalLeavesTheSecondFactorUnknown() throws IOException {
        status.set(401);

        assertThat(withKeycloak(account -> account.secondFactor("Bearer access-1"))).isEmpty();
    }

    @Test
    void theLanguageIsStoredOnTheAccountAndNothingElseChanges() throws IOException {
        body.set("{\"email\":\"jan@example.com\",\"attributes\":{\"locale\":[\"pl\"],\"terms\":[\"1\"]}}");

        withKeycloak(account -> {
            account.keepLanguage("Bearer access-1", "en");
            return Optional.empty();
        });

        assertThat(written).singleElement()
            .satisfies(
                account -> assertThat(account)
                    .contains("\"email\":\"jan@example.com\"", "\"terms\":[\"1\"]", "\"locale\":[\"en\"]")
            );
    }

    @Test
    void aLanguageTheAccountAlreadyHasIsNotWrittenAgain() throws IOException {
        body.set("{\"attributes\":{\"locale\":[\"en\"]}}");

        withKeycloak(account -> {
            account.keepLanguage("Bearer access-1", "en");
            return Optional.empty();
        });

        assertThat(written).isEmpty();
    }

    private <T> Optional<T> withKeycloak(Function<KeycloakAccount, Optional<T>> call) throws IOException {
        HttpServer keycloak = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        keycloak.createContext(ACCOUNT, exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            if ("POST".equals(exchange.getRequestMethod())) {
                written.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        keycloak.start();
        try {
            String url = "http://localhost:" + keycloak.getAddress().getPort() + ACCOUNT;
            return call
                .apply(new KeycloakAccount(url, new AuthTokenProvider(request -> null), JsonMapper.builder().build()));
        } finally {
            keycloak.stop(0);
        }
    }
}
