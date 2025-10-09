package com.vertyll.fastprod.common.exception;

import com.vertyll.fastprod.common.response.ApiResponse;
import com.vertyll.fastprod.common.response.ValidationErrorResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return ApiResponse.buildResponse(null, ex.getMessage(), ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";

            if (!errors.containsKey(field)) {
                errors.put(field, new ArrayList<>());
            }
            errors.get(field).add(message);
        });

        ValidationErrorResponse response = ValidationErrorResponse
                .builder()
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ignoredEx) {
        return ApiResponse.buildResponse(null, "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException ignoredEx) {
        return ApiResponse.buildResponse(null, "Account is disabled", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockedException(LockedException ignoredEx) {
        return ApiResponse.buildResponse(null, "Account is locked", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ignoredEx) {
        return ApiResponse.buildResponse(null, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
