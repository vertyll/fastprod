plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

val flywayVersion = "11.16.0"
val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val springdocVersion = "2.3.0"
val testcontainersVersion = "1.21.3"

dependencies {
    // Implementation dependencies - internal modules
    implementation(project(":modules:auth"))
    implementation(project(":modules:config"))
    implementation(project(":modules:employee"))
    implementation(project(":modules:role"))
    implementation(project(":modules:user"))

    // Implementation dependencies - Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Implementation dependencies - Thymeleaf
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Implementation dependencies - Database migration
    implementation("org.springframework.boot:spring-boot-starter-flyway") {
        exclude(group = "org.flywaydb", module = "flyway-core")
    }
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // Implementation dependencies - OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")

    // Runtime-only dependencies
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // Test compile-only dependencies
    testCompileOnly("org.projectlombok:lombok")

    // Test annotation processors
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test implementation dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
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
