plugins {
    id("java-library")
}

val guavaVersion = "33.5.0-jre"

dependencies {
    // API dependencies (exposed to consumers)
    api(project(":modules:common"))
    api("org.springframework.boot:spring-boot-starter-webmvc")

    // Implementation dependencies (internal only)
    implementation("org.apache.commons:commons-lang3")
    implementation("com.google.guava:guava:$guavaVersion")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok")

    // Test compile-only dependencies
    testCompileOnly("org.projectlombok:lombok")

    // Test annotation processors
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test implementation dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}
