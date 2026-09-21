package com.vertyll.fastprod.auth.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import lombok.Builder;

@Builder
public record SessionInfoDto(
    Long id,
    @Nullable String deviceInfo,
    String ipAddress,
    String userAgent,
    LocalDateTime createdAt,
    Instant lastUsedAt,
    Instant expiresAt
) {
}
