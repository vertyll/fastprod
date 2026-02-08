package com.vertyll.fastprod.sharedinfrastructure.response;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiResponseTest {

    @Test
    void buildResponse_WithAllParameters_ShouldCreateCorrectResponse() {
        // given
        String data = "test data";
        String message = "test message";
        HttpStatus status = HttpStatus.OK;

        // when
        ResponseEntity<ApiResponse<String>> response =
                ApiResponse.buildResponse(data, message, status);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<String> body = response.getBody();
        assertNotNull(body);
        assertEquals(data, body.getData());
        assertEquals(message, body.getMessage());
        assertNotNull(body.getTimestamp());
        assertInstanceOf(BaseResponse.class, body);
        assertInstanceOf(IResponse.class, body);
    }

    @Test
    void buildResponse_WithNullData_ShouldCreateResponseWithNullData() {
        // when
        ResponseEntity<ApiResponse<String>> response =
                ApiResponse.buildResponse(null, "message", HttpStatus.OK);

        // then
        assertNotNull(response);

        ApiResponse<String> body = response.getBody();
        assertNotNull(body);
        assertNull(body.getData());
    }

    @Test
    void constructor_ShouldSetDefaultTimestamp() {
        // when
        ApiResponse<String> response =
                ApiResponse.<String>builder().timestamp(LocalDateTime.now(ZoneOffset.UTC)).build();

        // then
        assertNotNull(response.getTimestamp());
        assertTrue(
                response.getTimestamp().isBefore(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1)));
        assertTrue(
                response.getTimestamp().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1)));
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
