package com.vertyll.fastprod.shared.service;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.shared.dto.PageResponse;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public abstract class BaseHttpService {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String COMMUNICATION_ERROR = "Error occurred during server communication";

    protected final String backendUrl;
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    private final AuthTokenProvider authTokenProvider;

    protected BaseHttpService(String backendUrl, ObjectMapper objectMapper, AuthTokenProvider authTokenProvider) {
        this.backendUrl = backendUrl;
        this.httpClient =
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).cookieHandler(new CookieManager()).build();
        this.objectMapper = objectMapper;
        this.authTokenProvider = authTokenProvider;
    }

    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    protected <T> @Nullable T get(String endpoint, Class<T> responseType) {
        return send(request(endpoint).GET(), objectMapper.constructType(responseType));
    }

    protected <R> @Nullable R post(String endpoint, Class<R> responseType) {
        return send(request(endpoint).POST(HttpRequest.BodyPublishers.noBody()), objectMapper.constructType(responseType));
    }

    protected <T, R> @Nullable R post(String endpoint, T requestBody, Class<R> responseType) {
        return send(
            request(endpoint).header(CONTENT_TYPE, APPLICATION_JSON).POST(json(requestBody)),
            objectMapper.constructType(responseType)
        );
    }

    protected <T, R> @Nullable R put(String endpoint, T requestBody, Class<R> responseType) {
        return send(
            request(endpoint).header(CONTENT_TYPE, APPLICATION_JSON).PUT(json(requestBody)),
            objectMapper.constructType(responseType)
        );
    }

    protected <T> @Nullable T delete(String endpoint, Class<T> responseType) {
        return send(request(endpoint).DELETE(), objectMapper.constructType(responseType));
    }

    protected <T> PageResponse<T> getPaginated(String endpoint, Class<T> responseType) {
        PageResponse<T> page = send(
            request(endpoint).GET(),
            objectMapper.getTypeFactory().constructParametricType(PageResponse.class, responseType)
        );
        if (page == null) {
            throw new ApiException(COMMUNICATION_ERROR);
        }
        return page;
    }

    private HttpRequest.Builder request(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(backendUrl + endpoint));
        authTokenProvider.getAuthorizationHeader().ifPresent(header -> builder.header("Authorization", header));
        return builder;
    }

    private HttpRequest.BodyPublisher json(Object body) {
        return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
    }

    private <T> @Nullable T send(HttpRequest.Builder builder, JavaType responseType) {
        HttpResponse<String> response;
        try {
            response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException(COMMUNICATION_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(COMMUNICATION_ERROR, e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw toApiException(response);
        }
        if (responseType.hasRawClass(Void.class) || response.body().isBlank()) {
            return null;
        }
        return objectMapper.readValue(response.body(), responseType);
    }

    private ApiException toApiException(HttpResponse<String> response) {
        log.warn("HTTP request failed with status {}", response.statusCode());
        try {
            Problem problem = objectMapper.readValue(response.body(), Problem.class);
            String detail = problem.detail() != null ? problem.detail()
                    : "Server error (code: " + response.statusCode() + ")";
            return new ApiException(detail, response.statusCode(), problem.fieldErrors());
        } catch (JacksonException e) {
            return new ApiException(COMMUNICATION_ERROR, response.statusCode());
        }
    }

    private record Problem(@Nullable String detail, @Nullable Map<String, List<String>> errors) {

        Map<String, List<String>> fieldErrors() {
            return errors == null ? Map.of() : errors;
        }
    }
}
