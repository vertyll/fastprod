package com.vertyll.fastprod.common.response;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class ValidationErrorResponseTest {

    @Test
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    void shouldBuildValidationErrorResponseCorrectly() {
        // given
        String testMessage = "Validation failed";
        Map<String, List<String>> testErrors = new HashMap<>();
        testErrors.put("username", List.of("Username cannot be empty"));
        testErrors.put("email", List.of("Invalid email format"));
        LocalDateTime testTime = LocalDateTime.now(ZoneOffset.UTC);

        // when
        ValidationErrorResponse response =
                ValidationErrorResponse.builder()
                        .message(testMessage)
                        .timestamp(testTime)
                        .errors(testErrors)
                        .build();

        // then
        assertEquals(testMessage, response.getMessage());
        assertEquals(testTime, response.getTimestamp());
        assertEquals(testErrors, response.getErrors());
        assertEquals(2, response.getErrors().size());
        assertEquals(List.of("Username cannot be empty"), response.getErrors().get("username"));
        assertEquals(List.of("Invalid email format"), response.getErrors().get("email"));
        assertNull(response.getData());
    }

    @Test
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    void shouldHandleMultipleErrorsForSameField() {
        // given
        String testMessage = "Validation failed";
        Map<String, List<String>> testErrors = new HashMap<>();
        testErrors.put(
                "password",
                List.of(
                        "Password must contain at least 8 characters",
                        "Password must contain at least one uppercase letter"));

        // when
        ValidationErrorResponse response =
                ValidationErrorResponse.builder().message(testMessage).errors(testErrors).build();

        // then
        assertEquals(1, response.getErrors().size());
        assertEquals(2, Objects.requireNonNull(response.getErrors().get("password")).size());
        assertTrue(
                Objects.requireNonNull(response.getErrors().get("password"))
                        .contains("Password must contain at least 8 characters"));
        assertTrue(
                Objects.requireNonNull(response.getErrors().get("password"))
                        .contains("Password must contain at least one uppercase letter"));
    }

    @Test
    void shouldInheritFromBaseResponse() {
        // given
        ValidationErrorResponse response =
                ValidationErrorResponse.builder().message("Error").build();

        // then
        assertNotNull(response);
    }
}
