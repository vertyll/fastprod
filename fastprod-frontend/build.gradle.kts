import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.9.6"
    id("com.diffplug.spotless") version "8.1.0"
    id("net.ltgt.errorprone") version "4.3.0"
    java
}

group = "com.vertyll.fastprod"
version = "1.0-SNAPSHOT"
description = "Production management system - front-end"

extra["author"] = "Mikołaj Gawron"
extra["email"] = "gawrmiko@gmail.com"

// Dependency versions
val springBootVersion = "3.5.9"
val vaadinVersion = "24.9.6"
val errorProneVersion = "2.36.0"
val nullawayVersion = "0.12.14"
val jspecifyVersion = "1.0.0"
val googleJavaFormatVersion = "1.33.0"
val ktlintVersion = "1.8.0"

repositories {
    mavenCentral()
    maven("https://maven.vaadin.com/vaadin-addons")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
    }
}

dependencies {
    // Implementation dependencies - Vaadin and Spring Boot
    implementation("com.vaadin:vaadin-core")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Compile-only dependencies
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.jspecify:jspecify:$jspecifyVersion")

    // Annotation processors
    annotationProcessor("org.projectlombok:lombok")

    // Error Prone dependencies
    "errorprone"("com.google.errorprone:error_prone_core:$errorProneVersion")
    "errorprone"("com.uber.nullaway:nullaway:$nullawayVersion")

    // Test implementation dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Test runtime-only dependencies
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")

    options.errorprone.isEnabled.set(true)

    options.errorprone {
        check("NullAway", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:CustomContractAnnotations", "org.springframework.lang.Contract")
        option("NullAway:JSpecifyMode", "true")

        excludedPaths.set(".*/build/generated/.*")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        target("src/**/*.java")
        googleJavaFormat(googleJavaFormatVersion).aosp().reflowLongStrings()
        removeUnusedImports()
        endWithNewline()
        trimTrailingWhitespace()
    }

    kotlin {
        target("**/*.gradle.kts")
        ktlint(ktlintVersion)
    }
}

tasks.named("bootRun").configure {
    dependsOn("vaadinPrepareFrontend")
}

tasks.named("build").configure {
    dependsOn("vaadinBuildFrontend")
}
