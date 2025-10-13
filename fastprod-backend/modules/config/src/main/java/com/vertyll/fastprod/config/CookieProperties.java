package com.vertyll.fastprod.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.cookie")
@Getter
@Setter
public class CookieProperties {
    private boolean httpOnly = true;
    private boolean secure = true;
    private String sameSite = "Strict";
}
