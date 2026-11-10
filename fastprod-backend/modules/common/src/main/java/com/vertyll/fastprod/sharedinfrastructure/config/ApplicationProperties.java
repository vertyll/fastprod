package com.vertyll.fastprod.sharedinfrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application")
@Validated
public record ApplicationProperties(@Valid @NotNull Frontend frontend, @Valid @NotNull Mail mail) {
    public record Frontend(@NotBlank(message = "Frontend URL is required") String url) {
    }

    public record Mail(@NotBlank(message = "Mail sender address is required") String from) {
    }
}
