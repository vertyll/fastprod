plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Implementation - Internal Modules
    implementation(project(":modules:auth"))
    implementation(project(":modules:config"))
    implementation(project(":modules:employee"))
    implementation(project(":modules:role"))
    implementation(project(":modules:user"))

    // Implementation - Spring Boot Starters
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)

    // Implementation - Thymeleaf
    implementation(libs.thymeleaf.extras.springsecurity6)

    // Implementation - Database Migration
    implementation(libs.spring.boot.starter.flyway) {
        exclude(group = "org.flywaydb", module = "flyway-core")
    }
    implementation(libs.bundles.flyway)

    // Implementation - OpenAPI/Swagger
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.mapstruct)

    // Compile Only
    compileOnly(libs.lombok)

    // Runtime Only
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.spring.boot.devtools)

    // Annotation Processor
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.bundles.mapstruct.processors)

    // Test Compile Only
    testCompileOnly(libs.lombok)

    // Test Annotation Processor
    testAnnotationProcessor(libs.lombok)

    // Test Implementation
    testImplementation(libs.bundles.test.starters)
    testImplementation(libs.h2)
    testImplementation(libs.bundles.testcontainers)
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
