package com.vertyll.fastprod.common.response;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorResponseTest {

    @Test
    void shouldBuildValidationErrorResponseCorrectly() {
        // given
        String testMessage = "Validation failed";
        Map<String, String> testErrors = new HashMap<>();
        testErrors.put("username", "Username cannot be empty");
        testErrors.put("email", "Invalid email format");
        LocalDateTime testTime = LocalDateTime.now();

        // when
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .message(testMessage)
                .timestamp(testTime)
                .errors(testErrors)
                .build();

        // then
        assertEquals(testMessage, response.getMessage());
        assertEquals(testTime, response.getTimestamp());
        assertEquals(testErrors, response.getErrors());
        assertEquals(2, response.getErrors().size());
        assertEquals("Username cannot be empty", response.getErrors().get("username"));
        assertEquals("Invalid email format", response.getErrors().get("email"));
        assertNull(response.getData());
    }

    @Test
    void shouldInheritFromBaseResponse() {
        // given
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .message("Error")
                .build();

        // then
        assertTrue(response instanceof BaseResponse);
    }
}