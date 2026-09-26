plugins {
    id("java-library")
}

dependencies {
    api(project(":modules:common"))
    api(project(":modules:auth"))

    api(libs.bundles.spring.boot.starters.security)
    api(libs.spring.boot.starter.webmvc)
    api(libs.spring.boot.starter.data.jpa)

    implementation(libs.jjwt.api)

    compileOnly(libs.lombok)

    runtimeOnly(libs.bundles.jjwt)

    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.bundles.spring.boot.test.security)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
}
