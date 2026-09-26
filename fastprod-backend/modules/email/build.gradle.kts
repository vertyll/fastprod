plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))

    api(libs.bundles.spring.boot.starters.mail)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.bundles.spring.boot.test.mail)
}
