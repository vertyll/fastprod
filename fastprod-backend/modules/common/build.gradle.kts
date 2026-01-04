plugins {
    id("java-library")
}

val mapstructVersion = "1.6.3"
val guavaVersion = "33.5.0-jre"

dependencies {
    // API dependencies (exposed to consumers)
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-webmvc")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.mapstruct:mapstruct:$mapstructVersion")

    // Implementation dependencies (internal only)
    implementation("com.google.guava:guava:$guavaVersion")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Test compile-only dependencies
    testCompileOnly("org.projectlombok:lombok")

    // Test annotation processors
    testAnnotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Test implementation dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
}
