plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))
    api(project(":modules:role"))
    api(project(":modules:user"))

    api(libs.bundles.spring.boot.starters.common)
    api(libs.bundles.spring.boot.starters.security)
    api(libs.mapstruct)

    compileOnly(libs.lombok)
    compileOnly(libs.springdoc.openapi.starter.webmvc.ui)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.bundles.mapstruct.processors)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.bundles.mapstruct.processors)

    testImplementation(libs.bundles.spring.boot.test.common)
    testImplementation(libs.bundles.spring.boot.test.security)
}
