plugins {
    id("java-library")
}

dependencies {
    // API - Internal Modules
    api(project(":modules:common"))

    // API
    api(libs.spring.boot.starter.mail)
    api(libs.spring.boot.starter.thymeleaf)
    api(libs.thymeleaf.extras.springsecurity6)

    // Compile Only
    compileOnly(libs.lombok)

    // Annotation Processor
    annotationProcessor(libs.lombok)

    // Test Compile Only
    testCompileOnly(libs.lombok)

    // Test Annotation Processor
    testAnnotationProcessor(libs.lombok)

    // Test Implementation
    testImplementation(libs.spring.boot.starter.mail.test)
    testImplementation(libs.spring.boot.starter.thymeleaf.test)
}
