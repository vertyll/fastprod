package com.vertyll.fastprod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.vertyll.fastprod")
@EnableScheduling
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot application class must be instantiable
public class FastprodBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastprodBackendApplication.class, args);
    }
}
