plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    // Internal dependencies
    implementation(project(":modules:auth"))
    implementation(project(":modules:config"))
    implementation(project(":modules:employee"))
    implementation(project(":modules:role"))
    implementation(project(":modules:user"))

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Thymeleaf and Spring Security integration
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Database migration
    implementation("org.springframework.boot:spring-boot-starter-flyway") {
        exclude(group = "org.flywaydb", module = "flyway-core")
    }
    implementation("org.flywaydb:flyway-core:11.16.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.16.0")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // Ensure Lombok and MapStruct work together during annotation processing
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Runtime
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:postgresql:1.21.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
