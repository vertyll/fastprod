package com.vertyll.fastprod;

import java.io.Serial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.vertyll.fastprod.shared.config.BackendApiProperties;
import com.vertyll.fastprod.shared.config.KeycloakProperties;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.lumo.Lumo;

@SpringBootApplication
@EnableConfigurationProperties(
    {
        KeycloakProperties.class,
        BackendApiProperties.class
    }
)
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
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
