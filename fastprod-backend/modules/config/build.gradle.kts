plugins {
    id("java-library")
}

val springdocVersion = "2.3.0"
val jjwtVersion = "0.12.3"

dependencies {
    // API dependencies (exposed to consumers)
    api(project(":modules:common"))
    api(project(":modules:auth"))
    api(project(":modules:role"))
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-webmvc")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Implementation dependencies (internal only)
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Runtime-only dependencies
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok")

    // Test compile-only dependencies
    testCompileOnly("org.projectlombok:lombok")

    // Test annotation processors
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test implementation dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
}
