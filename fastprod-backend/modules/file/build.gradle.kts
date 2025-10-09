plugins {
    id("java-library")
}

dependencies {
    // Internal dependencies
    api(project(":modules:common"))

    // Spring dependencies
    api("org.springframework.boot:spring-boot-starter-web")

    // Apache Commons
    implementation("org.apache.commons:commons-lang3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
