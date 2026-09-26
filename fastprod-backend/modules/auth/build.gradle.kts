plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))
    api(project(":modules:user"))
    api(project(":modules:email"))

    api(libs.bundles.spring.boot.starters.common)
    api(libs.bundles.spring.boot.starters.security)
    api(libs.mapstruct)

    implementation(libs.jjwt.api)
    implementation(libs.guava)

    compileOnly(libs.lombok)
    compileOnly(libs.springdoc.openapi.starter.webmvc.ui)

    runtimeOnly(libs.bundles.jjwt)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.bundles.mapstruct.processors)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.bundles.mapstruct.processors)

    testImplementation(libs.bundles.spring.boot.test.common)
    testImplementation(libs.bundles.spring.boot.test.security)
}
