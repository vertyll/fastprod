plugins {
    id("java-library")
}

dependencies {
    api(libs.bundles.spring.boot.starters.common)
    api(libs.bundles.spring.boot.starters.mail)
    api(libs.bundles.spring.boot.starters.security)
    api(libs.mapstruct)
    api(libs.jspecify)

    implementation(libs.guava)

    compileOnly(libs.lombok)
    compileOnly(libs.springdoc.openapi.starter.webmvc.ui)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.mapstruct.processor)

    testImplementation(libs.bundles.spring.boot.test.common)
    testImplementation(libs.bundles.spring.boot.test.mail)
    testImplementation(libs.bundles.spring.boot.test.security)
}
