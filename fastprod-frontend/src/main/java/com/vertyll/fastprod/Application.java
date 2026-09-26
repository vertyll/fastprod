package com.vertyll.fastprod;

import java.io.Serial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.vertyll.fastprod.shared.config.SecurityProperties;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class Application implements AppShellConfigurator {
    @Serial
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "favicon.ico", "any");
        settings.addLink("shortcut icon", "favicon.ico");
    }
}
