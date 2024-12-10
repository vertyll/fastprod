package com.vertyll.fastprod.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @SuppressWarnings("null")
    @Test
    void buildResponse_WithAllParameters_ShouldCreateCorrectResponse() {
        // given
        String data = "test data";
        String message = "test message";
        HttpStatus status = HttpStatus.OK;

        // when
        ResponseEntity<ApiResponse<String>> response = ApiResponse.buildResponse(data, message, status);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(data, response.getBody().getData());
        assertEquals(message, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void buildResponse_WithNullData_ShouldCreateResponseWithNullData() {
        // when
        ResponseEntity<ApiResponse<String>> response = ApiResponse.buildResponse(
                null,
                "message",
                HttpStatus.OK
        );

        // then
        assertNotNull(response);
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void constructor_ShouldSetDefaultTimestamp() {
        // when
        ApiResponse<String> response = new ApiResponse<>();

        // then
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(response.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
}