package com.vertyll.fastprod.common.exception;

import com.vertyll.fastprod.common.response.ApiResponse;
import com.vertyll.fastprod.common.response.ValidationErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleApiException_ShouldReturnCorrectResponse() {
        // given
        ApiException ex = new ApiException("test message", HttpStatus.BAD_REQUEST);

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("test message", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleValidationException_ShouldReturnValidationErrorResponse() {
        // given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "username", "Username is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // when
        ResponseEntity<ValidationErrorResponse> response = handler.handleValidationException(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", Objects.requireNonNull(response.getBody()).getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertNotNull(response.getBody().getErrors());

        Map<String, String> errors = response.getBody().getErrors();
        assertEquals(1, errors.size());
        assertEquals("Username is required", errors.get("username"));
    }

    @Test
    void handleBadCredentialsException_ShouldReturnUnauthorized() {
        // given
        BadCredentialsException ex = new BadCredentialsException("bad credentials");

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleBadCredentialsException(ex);

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleDisabledException_ShouldReturnForbidden() {
        // given
        DisabledException ex = new DisabledException("disabled");

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleDisabledException(ex);

        // then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account is disabled", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleLockedException_ShouldReturnForbidden() {
        // given
        LockedException ex = new LockedException("locked");

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleLockedException(ex);

        // then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account is locked", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleException_ShouldReturnInternalServerError() {
        // given
        Exception ex = new RuntimeException("unexpected error");

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleException(ex);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", Objects.requireNonNull(response.getBody()).getMessage());
    }
}