package com.vertyll.fastprod.shared.exception;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

@Getter
public final class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_STATUS_CODE = 500;

    private final int statusCode;
    private final transient Map<String, List<String>> fieldErrors;

    public ApiException(String message, int statusCode, Map<String, List<String>> fieldErrors) {
        super(Objects.requireNonNull(message, "message"));
        this.statusCode = statusCode;
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public ApiException(String message, int statusCode) {
        this(message, statusCode, Map.of());
    }

    public ApiException(String message) {
        this(message, DEFAULT_STATUS_CODE);
    }

    public ApiException(String message, Throwable cause) {
        super(Objects.requireNonNull(message, "message"), cause);
        this.statusCode = DEFAULT_STATUS_CODE;
        this.fieldErrors = Map.of();
    }

    @Override
    public String getMessage() {
        return Objects.requireNonNull(super.getMessage());
    }
}
