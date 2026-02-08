package com.vertyll.fastprod.sharedinfrastructure.response;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

public interface IResponse<T> {
    @Nullable T getData();

    @Nullable String getMessage();

    LocalDateTime getTimestamp();
}
