package com.vertyll.fastprod.sharedinfrastructure.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
