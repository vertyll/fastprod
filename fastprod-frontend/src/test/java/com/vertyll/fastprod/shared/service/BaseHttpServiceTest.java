package com.vertyll.fastprod.shared.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vertyll.fastprod.shared.dto.PageResponse;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseHttpServiceTest {

    private HttpServer server;
    private TestService service;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        respond("/item", 200, "application/json", "{\"name\":\"Anna\"}");
        respond("/empty", 204, "application/json", "");
        respond("/page", 200, "application/json", """
                {"content":[{"name":"Anna"}],"pageNumber":0,"pageSize":10,"totalElements":1,
                 "totalPages":1,"first":true,"last":true,"empty":false}""");
        respond("/invalid", 400, "application/problem+json", """
                {"status":400,"title":"Bad Request","detail":"Validation failed",
                 "errors":{"email":["Email should be valid"]}}""");
        respond("/broken", 500, "text/html", "<html>oops</html>");
        server.start();

        String baseUrl = "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
        service = new TestService(baseUrl);
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void readsTheResponseBodyAsTheDto() {
        assertEquals(new Item("Anna"), service.get("/item", Item.class));
    }

    @Test
    void returnsNullForNoContent() {
        assertNull(service.post("/empty", Void.class));
    }

    @Test
    void readsAPage() {
        PageResponse<Item> page = service.getPaginated("/page", Item.class);

        assertEquals(List.of(new Item("Anna")), page.content());
        assertEquals(1L, page.totalElements());
    }

    @Test
    void turnsAProblemDetailIntoAnApiExceptionWithFieldErrors() {
        ApiException exception = assertThrows(ApiException.class, () -> service.get("/invalid", Item.class));

        assertEquals(400, exception.getStatusCode());
        assertEquals("Validation failed", exception.getMessage());
        assertEquals(Map.of("email", List.of("Email should be valid")), exception.getFieldErrors());
    }

    @Test
    void reportsACommunicationErrorWhenTheBodyIsNotAProblemDetail() {
        ApiException exception = assertThrows(ApiException.class, () -> service.get("/broken", Item.class));

        assertEquals(500, exception.getStatusCode());
        assertEquals("errors.common.communication", exception.getMessage());
    }

    private void respond(String path, int status, String contentType, String body) {
        server.createContext(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    record Item(String name) {
    }

    private static final class TestService extends BaseHttpService {

        TestService(String baseUrl) {
            super(baseUrl, JsonMapper.builder().build(), new AuthTokenProvider());
        }
    }
}
