package com.vertyll.fastprod.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

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
        assertInstanceOf(BaseResponse.class, response.getBody());
        assertInstanceOf(IResponse.class, response.getBody());
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
        ApiResponse<String> response = ApiResponse.<String>builder()
                .timestamp(LocalDateTime.now())
                .build();

        // then
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(response.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void response_ShouldImplementInterfaces() {
        // when
        ApiResponse<String> response = ApiResponse.<String>builder().build();

        // then
        assertInstanceOf(IResponse.class, response);
        assertInstanceOf(BaseResponse.class, response);
    }
}