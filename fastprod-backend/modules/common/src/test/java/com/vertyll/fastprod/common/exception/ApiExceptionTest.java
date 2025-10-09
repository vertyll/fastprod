package com.vertyll.fastprod.common.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionTest {

    @Test
    void constructor_ShouldSetMessageAndStatus() {
        // given
        String message = "Test error message";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // when
        ApiException exception = new ApiException(message, status);

        // then
        assertEquals(message, exception.getMessage());
        assertEquals(status, exception.getStatus());
    }

    @Test
    void getStatus_ShouldReturnCorrectStatus() {
        // when
        ApiException exception = new ApiException("message", HttpStatus.NOT_FOUND);

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        // when
        ApiException exception = new ApiException("test message", HttpStatus.BAD_REQUEST);

        // then
        assertEquals("test message", exception.getMessage());
    }
}
