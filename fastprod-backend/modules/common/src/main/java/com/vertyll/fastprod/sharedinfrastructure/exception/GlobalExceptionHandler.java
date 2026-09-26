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
    public static final String CODE_PROPERTY = "code";
    public static final String ARGS_PROPERTY = "args";

    private static final String AN_UNEXPECTED_ERROR_OCCURRED = "errors.common.unexpected";
    private static final String INVALID_VALUE = "validation.invalid";
    private static final String VALIDATION_FAILED = "errors.common.validationFailed";
    private static final String INVALID_EMAIL_OR_PASSWORD = "errors.auth.invalidCredentials";
    private static final String ACCOUNT_IS_DISABLED = "errors.auth.accountDisabled";
    private static final String ACCOUNT_IS_LOCKED = "errors.auth.accountLocked";
    private static final String NOT_HAVE_PERMISSION_TO_PERFORM_THIS_ACTION = "errors.auth.forbidden";
    private static final String ACCESS_DENIED = "errors.auth.accessDenied";
    private static final String AUTHENTICATION_REQUIRED = "errors.auth.authenticationRequired";

    public static ProblemDetail problem(HttpStatusCode status, String messageKey) {
        return problem(status, messageKey, Map.of());
    }

    public static ProblemDetail problem(HttpStatusCode status, String messageKey, Map<String, ?> args) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, messageKey);
        problem.setProperty(CODE_PROPERTY, messageKey);
        if (!args.isEmpty()) {
            problem.setProperty(ARGS_PROPERTY, args);
        }
        return problem;
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex) {
        return problem(ex.getStatus(), requireNonNullElse(ex.getMessage(), AN_UNEXPECTED_ERROR_OCCURRED), ex.getArgs());
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
