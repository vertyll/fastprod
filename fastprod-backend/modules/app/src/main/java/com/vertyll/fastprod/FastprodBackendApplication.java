package com.vertyll.fastprod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.vertyll.fastprod")
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot application class must be instantiable
public class FastprodBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastprodBackendApplication.class, args);
    }
}
