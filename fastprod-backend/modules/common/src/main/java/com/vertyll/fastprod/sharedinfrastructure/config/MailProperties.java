package com.vertyll.fastprod.sharedinfrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.mail")
public record MailProperties(String from) {
}
