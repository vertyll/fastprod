package com.vertyll.fastprod.auth.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonFormat;

public record SessionResponseDto(
    Long id,
    @Nullable String deviceInfo,
    String ipAddress,
    String userAgent,
    String browser,
    String os,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC") LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC") Instant lastUsedAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC") Instant expiresAt,
    boolean isCurrent
) {
}
