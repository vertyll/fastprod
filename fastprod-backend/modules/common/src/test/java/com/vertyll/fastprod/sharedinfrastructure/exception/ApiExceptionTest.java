package com.vertyll.fastprod.sharedinfrastructure.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionTest {
    @Test
    void constructor_ShouldSetMessageAndStatus() {
        String message = "Test error message";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiException exception = new ApiException(message, status);

        assertEquals(message, exception.getMessage());
        assertEquals(status, exception.getStatus());
    }

    @Test
    void getStatus_ShouldReturnCorrectStatus() {
        ApiException exception = new ApiException("message", HttpStatus.NOT_FOUND);

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        ApiException exception = new ApiException("test message", HttpStatus.BAD_REQUEST);

        assertEquals("test message", exception.getMessage());
    }
}
