package com.vertyll.fastprod.sharedinfrastructure.exception;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    @SuppressWarnings("PMD.LooseCoupling")
    private final LinkedHashMap<String, Serializable> args;

    public ApiException(String messageKey, HttpStatus status) {
        this(messageKey, status, Map.of());
    }

    public ApiException(String messageKey, HttpStatus status, Map<String, ? extends Serializable> args) {
        super(messageKey);
        this.status = status;
        this.args = new LinkedHashMap<>(args);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Serializable> getArgs() {
        return Collections.unmodifiableMap(args);
    }
}
