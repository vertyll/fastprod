plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))

    api(libs.spring.boot.starter.webmvc)

    implementation(libs.commons.lang3)
    implementation(libs.guava)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.webmvc.test)
}
