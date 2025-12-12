plugins {
    id("java-library")
}

dependencies {
    // Internal dependencies
    api(project(":modules:common"))

    // Spring dependencies
    api("org.springframework.boot:spring-boot-starter-mail")
    api("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Thymeleaf and Spring Security integration
    api("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
}
