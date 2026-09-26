package com.vertyll.fastprod.sharedinfrastructure.exception;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNullElse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String ERRORS_PROPERTY = "errors";

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

    public static ProblemDetail problem(HttpStatusCode status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        return problem(ex.getStatus(), requireNonNullElse(ex.getMessage(), AN_UNEXPECTED_ERROR_OCCURRED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = requireNonNullElse(error.getDefaultMessage(), INVALID_VALUE);
            errors.computeIfAbsent(error.getField(), _ -> new ArrayList<>()).add(message);
        });

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, VALIDATION_FAILED);
        problem.setProperty(ERRORS_PROPERTY, errors);
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ignoredEx) {
        return problem(HttpStatus.UNAUTHORIZED, INVALID_EMAIL_OR_PASSWORD);
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabledException(DisabledException ignoredEx) {
        return problem(HttpStatus.FORBIDDEN, ACCOUNT_IS_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLockedException(LockedException ignoredEx) {
        return problem(HttpStatus.FORBIDDEN, ACCOUNT_IS_LOCKED);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException ignoredEx) {
        return problem(HttpStatus.FORBIDDEN, NOT_HAVE_PERMISSION_TO_PERFORM_THIS_ACTION);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ignoredEx) {
        return problem(HttpStatus.FORBIDDEN, ACCESS_DENIED);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ProblemDetail handleAuthenticationCredentialsNotFoundException(
        AuthenticationCredentialsNotFoundException ignoredEx
    ) {
        return problem(HttpStatus.UNAUTHORIZED, AUTHENTICATION_REQUIRED);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, AN_UNEXPECTED_ERROR_OCCURRED);
    }
}
