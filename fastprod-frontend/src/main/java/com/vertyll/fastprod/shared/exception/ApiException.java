package com.vertyll.fastprod.shared.exception;

import java.io.Serial;
import java.util.Objects;

import lombok.Getter;

public final class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_STATUS_CODE = 500;

    @Getter
    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(Objects.requireNonNull(message, "message"));
        this.statusCode = statusCode;
    }

    public ApiException(String message) {
        this(message, DEFAULT_STATUS_CODE);
    }

    @Override
    public String getMessage() {
        return Objects.requireNonNull(super.getMessage());
    }
}
