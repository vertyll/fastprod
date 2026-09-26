package com.vertyll.fastprod.sharedinfrastructure.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static java.util.Objects.requireNonNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        ApiException ex = new ApiException("test message", HttpStatus.BAD_REQUEST);

        ProblemDetail problem = handler.handleApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("test message", problem.getDetail());
    }

    @Test
    void handleValidationException_ShouldReturnFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "username", "Username is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ProblemDetail problem = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("errors.common.validationFailed", problem.getDetail());

        Map<String, List<String>> errors = errorsOf(problem);
        assertEquals(1, errors.size());
        assertEquals(List.of("Username is required"), errors.get("username"));
    }

    @Test
    void handleValidationException_ShouldHandleMultipleErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError passwordError1 = new FieldError("object", "password", "Password must be at least 8 characters");
        FieldError passwordError2 = new FieldError("object", "password", "Password must contain an uppercase letter");
        FieldError emailError = new FieldError("object", "email", "Invalid email format");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(passwordError1, passwordError2, emailError));

        ProblemDetail problem = handler.handleValidationException(ex);

        Map<String, List<String>> errors = errorsOf(problem);
        assertEquals(2, errors.size());
        assertEquals(
            List.of("Password must be at least 8 characters", "Password must contain an uppercase letter"),
            errors.get("password")
        );
        assertEquals(List.of("Invalid email format"), errors.get("email"));
    }

    @Test
    void handleBadCredentialsException_ShouldReturnUnauthorized() {
        ProblemDetail problem = handler.handleBadCredentialsException(new BadCredentialsException("bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
        assertEquals("errors.auth.invalidCredentials", problem.getDetail());
    }

    @Test
    void handleDisabledException_ShouldReturnForbidden() {
        ProblemDetail problem = handler.handleDisabledException(new DisabledException("disabled"));

        assertEquals(HttpStatus.FORBIDDEN.value(), problem.getStatus());
        assertEquals("errors.auth.accountDisabled", problem.getDetail());
    }

    @Test
    void handleLockedException_ShouldReturnForbidden() {
        ProblemDetail problem = handler.handleLockedException(new LockedException("locked"));

        assertEquals(HttpStatus.FORBIDDEN.value(), problem.getStatus());
        assertEquals("errors.auth.accountLocked", problem.getDetail());
    }

    @Test
    void handleException_ShouldReturnInternalServerError() {
        ProblemDetail problem = handler.handleException(new RuntimeException("unexpected error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("errors.common.unexpected", problem.getDetail());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> errorsOf(ProblemDetail problem) {
        return (Map<String, List<String>>) requireNonNull(
            requireNonNull(problem.getProperties()).get(GlobalExceptionHandler.ERRORS_PROPERTY)
        );
    }
}
