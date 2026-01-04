package com.vertyll.fastprod.common.exception;

import lombok.Getter;

import java.io.Serial;

import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
