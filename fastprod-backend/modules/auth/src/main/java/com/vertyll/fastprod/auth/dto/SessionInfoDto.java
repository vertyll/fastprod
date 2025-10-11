package com.vertyll.fastprod.auth.dto;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
public record SessionInfoDto(
        Long id,
        String deviceInfo,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt,
        Instant lastUsedAt,
        Instant expiresAt
) {
}
