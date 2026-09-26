plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))

    api(libs.bundles.spring.boot.starters.common)
    implementation(libs.icu4j)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.bundles.spring.boot.test.common)
}
