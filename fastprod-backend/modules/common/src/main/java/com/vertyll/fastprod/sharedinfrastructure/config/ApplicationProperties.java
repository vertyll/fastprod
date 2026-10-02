package com.vertyll.fastprod.sharedinfrastructure.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application")
@Validated
public record ApplicationProperties(Frontend frontend, Mail mail) {
    public record Frontend(@NotBlank(message = "Frontend URL is required") String url) {
    }

    public record Mail(@NotBlank(message = "Mail sender address is required") String from) {
    }
}
