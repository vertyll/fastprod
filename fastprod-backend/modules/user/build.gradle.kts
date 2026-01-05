plugins {
    id("java-library")
}

dependencies {
    // API - Internal Modules
    api(project(":modules:common"))
    api(project(":modules:role"))

    // API
    api(libs.bundles.spring.boot.starters.common)
    api(libs.bundles.spring.boot.starters.security)
    api(libs.spring.boot.starter.mail)
    api(libs.mapstruct)

    // Compile Only
    compileOnly(libs.lombok)
    compileOnly(libs.springdoc.openapi.starter.webmvc.ui)

    // Annotation Processor
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.bundles.mapstruct.processors)

    // Test Compile Only
    testCompileOnly(libs.lombok)

    // Test Annotation Processor
    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.bundles.mapstruct.processors)

    // Test Implementation
    testImplementation(libs.bundles.spring.boot.test.common)
    testImplementation(libs.bundles.spring.boot.test.security)
}
