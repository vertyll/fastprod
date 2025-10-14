package com.vertyll.fastprod.file.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.file.uploads")
@Validated
public record FileUploadProperties(
        @NotBlank(message = "File output path is required")
        String fileOutputPath
) {
}