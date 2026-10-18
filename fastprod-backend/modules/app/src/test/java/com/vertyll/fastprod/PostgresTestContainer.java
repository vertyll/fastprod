package com.vertyll.fastprod;

import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class PostgresTestContainer {

    // Spring stops the container when the test context closes.
    @Bean
    @SuppressWarnings("resource")
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:18-alpine").withStartupTimeout(Duration.ofMinutes(2))
            .withStartupAttempts(3);
    }
}
