plugins {
    id("java-library")
}

dependencies {
    // API - Internal Modules
    api(project(":modules:common"))
    api(project(":modules:auth"))

    // API
    api(libs.bundles.spring.boot.starters.security)
    api(libs.spring.boot.starter.webmvc)
    api(libs.spring.boot.starter.data.jpa)

    // Implementation
    implementation(libs.jjwt.api)

    // Compile Only
    compileOnly(libs.lombok)

    // Runtime Only
    runtimeOnly(libs.bundles.jjwt)

    // Annotation Processor
    annotationProcessor(libs.lombok)

    // Test Compile Only
    testCompileOnly(libs.lombok)

    // Test Annotation Processor
    testAnnotationProcessor(libs.lombok)

    // Test Implementation
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.bundles.spring.boot.test.security)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
}
