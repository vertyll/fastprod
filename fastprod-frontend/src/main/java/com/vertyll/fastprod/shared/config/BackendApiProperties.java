package com.vertyll.fastprod.shared.config;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Where the back-end is, and how long a call to it may take: {@code connectTimeout} to open the connection,
 * {@code readTimeout} for the whole answer.
 */
@Validated
@ConfigurationProperties(prefix = "application.backend")
public record BackendApiProperties(
    @NotBlank String url,
    @DefaultValue("5s") @NotNull Duration connectTimeout,
    @DefaultValue("30s") @NotNull Duration readTimeout
) {
}
