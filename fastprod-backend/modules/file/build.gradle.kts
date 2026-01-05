plugins {
    id("java-library")
}

dependencies {
    // API - Internal Modules
    api(project(":modules:common"))

    // API
    api(libs.spring.boot.starter.webmvc)

    // Implementation
    implementation(libs.commons.lang3)
    implementation(libs.guava)

    // Compile Only
    compileOnly(libs.lombok)

    // Annotation Processor
    annotationProcessor(libs.lombok)

    // Test Compile Only
    testCompileOnly(libs.lombok)

    // Test Annotation Processor
    testAnnotationProcessor(libs.lombok)

    // Test Implementation
    testImplementation(libs.spring.boot.starter.webmvc.test)
}
