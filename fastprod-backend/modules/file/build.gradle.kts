plugins {
    id("java-library")
}

dependencies {
    // Internal dependencies
    api(project(":modules:common"))

    // Spring dependencies
    api("org.springframework.boot:spring-boot-starter-webmvc")

    // Apache Commons
    implementation("org.apache.commons:commons-lang3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Guava
    implementation("com.google.guava:guava:33.5.0-jre")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}
