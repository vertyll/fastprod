package com.vertyll.fastprod.shared.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        T data,
        String message,
        LocalDateTime timestamp
) {
}
