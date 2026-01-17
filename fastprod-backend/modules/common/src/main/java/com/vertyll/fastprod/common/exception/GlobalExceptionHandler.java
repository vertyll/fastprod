package com.vertyll.fastprod.common.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vertyll.fastprod.common.response.ApiResponse;
import com.vertyll.fastprod.common.response.ValidationErrorResponse;

import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String AN_UNEXPECTED_ERROR_OCCURRED = "An unexpected error occurred";
    private static final String INVALID_VALUE = "Invalid value";
    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
    private static final String ACCOUNT_IS_DISABLED = "Account is disabled";
    private static final String ACCOUNT_IS_LOCKED = "Account is locked";
    private static final String NOT_HAVE_PERMISSION_TO_PERFORM_THIS_ACTION =
            "You do not have permission to perform this action";
    private static final String ACCESS_DENIED = "Access denied";
    private static final String AUTHENTICATION_REQUIRED = "Authentication required";
    public static final String INVALID_INPUT_FORMAT = "Invalid input format";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return ApiResponse.buildResponse(
                null,
                Objects.requireNonNullElse(ex.getMessage(), AN_UNEXPECTED_ERROR_OCCURRED),
                ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new ConcurrentHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(
                        error -> {
                            String field = error.getField();
                            String message =
                                    error.getDefaultMessage() != null
                                            ? error.getDefaultMessage()
                                            : INVALID_VALUE;

                            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
                        });

        ValidationErrorResponse response =
                ValidationErrorResponse.builder()
                        .message(VALIDATION_FAILED)
                        .errors(errors)
                        .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                        .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        String message = INVALID_INPUT_FORMAT;

        if (ex.getCause() instanceof InvalidFormatException ife
                && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            message =
                    String.format(
                            "Invalid value '%s' for type %s. Accepted values: %s",
                            ife.getValue(),
                            ife.getTargetType().getSimpleName(),
                            java.util.Arrays.toString(ife.getTargetType().getEnumConstants()));
        }

        return ApiResponse.buildResponse(null, message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ignoredEx) {
        return ApiResponse.buildResponse(null, INVALID_EMAIL_OR_PASSWORD, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException ignoredEx) {
        return ApiResponse.buildResponse(null, ACCOUNT_IS_DISABLED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockedException(LockedException ignoredEx) {
        return ApiResponse.buildResponse(null, ACCOUNT_IS_LOCKED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException ignoredEx) {
        return ApiResponse.buildResponse(null, AUTHENTICATION_REQUIRED, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationDeniedException(
            AuthorizationDeniedException ignoredEx) {
        return ApiResponse.buildResponse(
                null, NOT_HAVE_PERMISSION_TO_PERFORM_THIS_ACTION, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ignoredEx) {
        return ApiResponse.buildResponse(null, ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ignoredEx) {
        return ApiResponse.buildResponse(
                null, AN_UNEXPECTED_ERROR_OCCURRED, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
