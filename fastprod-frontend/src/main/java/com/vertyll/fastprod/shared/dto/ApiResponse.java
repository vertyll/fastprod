package com.vertyll.fastprod.shared.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        @Nullable  T data,
        String message,
        LocalDateTime timestamp
) {
}
