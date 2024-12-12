package com.vertyll.fastprod.common.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseResponse<T> implements IResponse<T> {
    protected T data;
    protected String message;
    protected LocalDateTime timestamp = LocalDateTime.now();
}