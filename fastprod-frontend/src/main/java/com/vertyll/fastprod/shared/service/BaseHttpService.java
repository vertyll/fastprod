package com.vertyll.fastprod.shared.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.dto.PageResponse;
import com.vertyll.fastprod.shared.dto.PaginatedApiResponse;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.security.AuthTokenProvider;
import lombok.extern.slf4j.Slf4j;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public abstract class BaseHttpService {

    protected final String backendUrl;
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    private final AuthTokenProvider authTokenProvider;

    protected BaseHttpService(String backendUrl, ObjectMapper objectMapper, AuthTokenProvider authTokenProvider) {
        this.backendUrl = backendUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
        this.objectMapper = objectMapper;
        this.authTokenProvider = authTokenProvider;
    }

    private void addAuthorizationHeader(HttpRequest.Builder requestBuilder) {
        if (authTokenProvider != null) {
            String authHeader = authTokenProvider.getAuthorizationHeader();
            if (authHeader != null) {
                requestBuilder.header("Authorization", authHeader);
            }
        }
    }

    protected <T> ApiResponse<T> get(String endpoint, Class<T> responseType) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .GET();

        addAuthorizationHeader(requestBuilder);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleResponse(response, responseType);
    }

    protected <T, R> ApiResponse<R> post(String endpoint, T requestBody, Class<R> responseType) throws Exception {
        String json = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "";

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        addAuthorizationHeader(requestBuilder);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleResponse(response, responseType);
    }

    protected <T, R> ApiResponse<R> put(String endpoint, T requestBody, Class<R> responseType) throws Exception {
        String json = objectMapper.writeValueAsString(requestBody);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json));

        addAuthorizationHeader(requestBuilder);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleResponse(response, responseType);
    }

    protected <T> ApiResponse<T> delete(String endpoint, Class<T> responseType) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .DELETE();

        addAuthorizationHeader(requestBuilder);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handleResponse(response, responseType);
    }

    protected <T> PageResponse<T> getPaginated(String endpoint, Class<T> responseType) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + endpoint))
                .GET();

        addAuthorizationHeader(requestBuilder);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return handlePaginatedResponse(response, responseType);
    }

    private <T> ApiResponse<T> handleResponse(HttpResponse<String> response, Class<T> responseType) throws Exception {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (responseType == Void.class) {
                return objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, Void.class));
            }
            return objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, responseType));
        } else {
            log.error("HTTP request failed with status: {}, body: {}", response.statusCode(), response.body());

            try {
                ApiResponse<?> errorResponse = objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, Object.class));
                String errorMessage = errorResponse.message() != null
                        ? errorResponse.message()
                        : "Server error (code: " + response.statusCode() + ")";
                throw new ApiException(errorMessage, response.statusCode());
            } catch (ApiException ae) {
                throw ae;
            } catch (Exception parseException) {
                log.error("Failed to parse error response", parseException);
                throw new ApiException("Error occurred during server communication", response.statusCode());
            }
        }
    }

    private <T> PageResponse<T> handlePaginatedResponse(HttpResponse<String> response, Class<T> responseType) throws Exception {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            PaginatedApiResponse<T> paginatedResponse = objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructParametricType(PaginatedApiResponse.class, responseType));
            return paginatedResponse.data();
        } else {
            log.error("HTTP request failed with status: {}, body: {}", response.statusCode(), response.body());

            try {
                ApiResponse<?> errorResponse = objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, Object.class));
                String errorMessage = errorResponse.message() != null
                        ? errorResponse.message()
                        : "Server error (code: " + response.statusCode() + ")";
                throw new ApiException(errorMessage, response.statusCode());
            } catch (ApiException ae) {
                throw ae;
            } catch (Exception parseException) {
                log.error("Failed to parse error response", parseException);
                throw new ApiException("Error occurred during server communication", response.statusCode());
            }
        }
    }
}
