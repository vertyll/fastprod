package com.vertyll.fastprod.sharedinfrastructure.config;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "spring.mail")
public record MailProperties(
    String host,
    @Nullable Integer port,
    String username,
    String password,
    String from,
    @DefaultValue Map<String, String> properties
) {
}
