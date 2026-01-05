package com.vertyll.fastprod.shared.dto;

import java.time.LocalDateTime;

public record PaginatedApiResponse<T>(
        PageResponse<T> data, String message, LocalDateTime timestamp) {}
