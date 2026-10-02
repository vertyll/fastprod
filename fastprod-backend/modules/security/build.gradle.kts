plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))

    api(libs.bundles.spring.boot.starters.security)
    api(libs.spring.boot.starter.webmvc)
    api(libs.spring.boot.starter.data.jpa)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.bundles.spring.boot.test.security)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
}
