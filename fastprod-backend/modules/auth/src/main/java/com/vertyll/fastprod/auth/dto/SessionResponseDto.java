package com.vertyll.fastprod.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
public record SessionResponseDto(
        Long id,
        String deviceInfo,
        String ipAddress,
        String userAgent,
        String browser,
        String os,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant lastUsedAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant expiresAt,
        boolean current
) {
    public static SessionResponseDto fromSessionInfo(
            SessionInfoDto info,
            boolean isCurrent
    ) {
        UserAgentParser parser = new UserAgentParser(info.userAgent());

        return SessionResponseDto.builder()
                .id(info.id())
                .deviceInfo(info.deviceInfo())
                .ipAddress(info.ipAddress())
                .userAgent(info.userAgent())
                .browser(parser.getBrowser())
                .os(parser.getOs())
                .createdAt(info.createdAt())
                .lastUsedAt(info.lastUsedAt())
                .expiresAt(info.expiresAt())
                .current(isCurrent)
                .build();
    }

    /**
     * Simple User-Agent parser
     */
    private record UserAgentParser(String userAgent) {
        private UserAgentParser(String userAgent) {
            this.userAgent = userAgent != null ? userAgent : "unknown";
        }

        String getBrowser() {
            if (userAgent.contains("Chrome")) return "Chrome";
            if (userAgent.contains("Firefox")) return "Firefox";
            if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
            if (userAgent.contains("Edge")) return "Edge";
            if (userAgent.contains("Opera") || userAgent.contains("OPR")) return "Opera";
            return "Unknown";
        }

        String getOs() {
            if (userAgent.contains("Windows")) return "Windows";
            if (userAgent.contains("Mac")) return "macOS";
            if (userAgent.contains("Linux")) return "Linux";
            if (userAgent.contains("Android")) return "Android";
            if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
            return "Unknown";
        }
    }
}
