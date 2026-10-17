package com.vertyll.fastprod.shared.security;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.vertyll.fastprod.shared.security.KeycloakAccount.SecondFactor;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakAccountTest {

    private static final String CREDENTIALS = "/realms/fastprod/account/credentials";

    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicReference<String> body = new AtomicReference<>("");

    @Test
    void theSecondFactorIsReadWithTheCallersOwnToken() throws IOException {
        body.set("""
                [{"type":"otp","category":"two-factor",
                  "userCredentialMetadatas":[{"credential":{"id":"otp-1","type":"otp","userLabel":"phone"}}]}]
                """);

        assertThat(withKeycloak()).contains(new SecondFactor("otp-1"));
        assertThat(authorization.get()).isEqualTo("Bearer access-1");
    }

    @Test
    void noOneTimePasswordIsASecondFactorTurnedOff() throws IOException {
        body.set("[{\"type\":\"otp\",\"category\":\"two-factor\",\"userCredentialMetadatas\":[]}]");

        assertThat(withKeycloak()).contains(new SecondFactor(null));
    }

    @Test
    void aRefusalLeavesTheSecondFactorUnknown() throws IOException {
        status.set(401);

        assertThat(withKeycloak()).isEmpty();
    }

    private Optional<SecondFactor> withKeycloak() throws IOException {
        HttpServer keycloak = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        keycloak.createContext(CREDENTIALS, exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        keycloak.start();
        try {
            String url = "http://localhost:" + keycloak.getAddress().getPort() + CREDENTIALS + "?type=otp";
            KeycloakAccount account =
                    new KeycloakAccount(url, new AuthTokenProvider(request -> null), JsonMapper.builder().build());
            return account.secondFactor("Bearer access-1");
        } finally {
            keycloak.stop(0);
        }
    }
}
