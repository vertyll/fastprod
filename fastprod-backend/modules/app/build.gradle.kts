plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":modules:security"))
    implementation(project(":modules:employee"))
    implementation(project(":modules:role"))
    implementation(project(":modules:user"))
    implementation(project(":modules:translation"))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.spring.boot.starter.flyway) {
        exclude(group = "org.flywaydb", module = "flyway-core")
    }
    implementation(libs.bundles.flyway)

    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.mapstruct)

    compileOnly(libs.lombok)

    runtimeOnly(libs.postgresql)
    developmentOnly(libs.spring.boot.devtools)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.bundles.mapstruct.processors)

    testCompileOnly(libs.lombok)

    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.bundles.test.starters)
    testImplementation(libs.bundles.testcontainers)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("fastprod-backend.jar")
}

tasks.named<Jar>("jar") {
    enabled = false
}
