package com.vertyll.fastprod.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.backend")
public record BackendApiProperties(String url) {
}
