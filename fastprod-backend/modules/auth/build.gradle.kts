plugins {
    id("java-library")
}

dependencies {
    // API - Internal Modules
    api(project(":modules:common"))
    api(project(":modules:user"))
    api(project(":modules:email"))

    // API - Spring Boot & MapStruct
    api(libs.bundles.spring.boot.starters.common)
    api(libs.bundles.spring.boot.starters.security)
    api(libs.mapstruct)

    // Implementation
    implementation(libs.jjwt.api)
    implementation(libs.guava)

    // Compile Only
    compileOnly(libs.lombok)
    compileOnly(libs.springdoc.openapi.starter.webmvc.ui)

    // Runtime Only
    runtimeOnly(libs.bundles.jjwt)

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
