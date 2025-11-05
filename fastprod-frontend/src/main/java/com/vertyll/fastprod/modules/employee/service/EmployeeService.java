package com.vertyll.fastprod.modules.employee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinSession;
import com.vertyll.fastprod.modules.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.dto.PageResponse;
import com.vertyll.fastprod.shared.dto.PaginatedApiResponse;
import com.vertyll.fastprod.shared.service.BaseHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@Slf4j
public class EmployeeService extends BaseHttpService {

    private static final String EMPLOYEE_ENDPOINT = "/employees";

    public EmployeeService(@Value("${api.backend.url}") String backendUrl, ObjectMapper objectMapper) {
        super(backendUrl, objectMapper);
    }

    private String getAuthToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return (String) session.getAttribute("token");
        }
        return null;
    }

    public void createEmployee(EmployeeCreateDto createDto) throws Exception {
        String json = objectMapper.writeValueAsString(createDto);
        String authToken = getAuthToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + EMPLOYEE_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, EmployeeResponseDto.class));
        } else {
            throw new Exception("Failed to create employee: " + response.body());
        }
    }

    public void updateEmployee(Long id, EmployeeUpdateDto updateDto) throws Exception {
        put(EMPLOYEE_ENDPOINT + "/" + id, updateDto, EmployeeResponseDto.class, getAuthToken());
    }

    public ApiResponse<EmployeeResponseDto> getEmployee(Long id) throws Exception {
        return get(EMPLOYEE_ENDPOINT + "/" + id, EmployeeResponseDto.class, getAuthToken());
    }

    public PageResponse<EmployeeResponseDto> getAllEmployees(int page, int size, String sortBy, String sortDirection)
            throws Exception {
        String authToken = getAuthToken();

        String url = String.format("%s%s?page=%d&size=%d&sortBy=%s&sortDirection=%s",
                backendUrl, EMPLOYEE_ENDPOINT, page, size, sortBy, sortDirection);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            PaginatedApiResponse<EmployeeResponseDto> apiResponse = objectMapper.readValue(response.body(),
                    objectMapper.getTypeFactory().constructParametricType(PaginatedApiResponse.class,
                            EmployeeResponseDto.class));
            return apiResponse.data();
        } else {
            throw new Exception("Failed to fetch employees: " + response.body());
        }
    }

    @Deprecated
    public List<EmployeeResponseDto> getAllEmployees() throws Exception {
        PageResponse<EmployeeResponseDto> pageResponse = getAllEmployees(0, 10, "id", "ASC");
        return pageResponse.content();
    }

    public void deleteEmployee(Long id) throws Exception {
        String authToken = getAuthToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + EMPLOYEE_ENDPOINT + "/" + id))
                .DELETE();

        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new Exception("Failed to delete employee: " + response.body());
        }
    }
}
