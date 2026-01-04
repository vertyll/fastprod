plugins {
    id("java-library")
}

val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val springdocVersion = "2.3.0"

dependencies {
    // API dependencies (exposed to consumers)
    api(project(":modules:common"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-webmvc")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.mapstruct:mapstruct:$mapstructVersion")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

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
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
}
