plugins {
    id("java-library")
}

val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val springdocVersion = "2.3.0"
val jjwtVersion = "0.12.3"
val guavaVersion = "33.5.0-jre"

dependencies {
    // API dependencies (exposed to consumers)
    api(project(":modules:common"))
    api(project(":modules:user"))
    api(project(":modules:email"))
    api("org.springframework.boot:spring-boot-starter-webmvc")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.mapstruct:mapstruct:$mapstructVersion")

    // Implementation dependencies (internal only)
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    implementation("com.google.guava:guava:$guavaVersion")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Runtime-only dependencies
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // Test compile-only dependencies
    testCompileOnly("org.projectlombok:lombok")

    // Test annotation processors
    testAnnotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // Test implementation dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
}
