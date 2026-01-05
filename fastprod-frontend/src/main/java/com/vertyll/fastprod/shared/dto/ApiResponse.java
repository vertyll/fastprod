package com.vertyll.fastprod.shared.dto;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

public record ApiResponse<T>(@Nullable T data, String message, LocalDateTime timestamp) {}
