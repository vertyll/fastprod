package com.vertyll.fastprod.common.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseResponse<T> implements IResponse<T> {
    protected @Nullable T data;
    protected String message;

    @Builder.Default
    protected LocalDateTime timestamp = LocalDateTime.now();
}
