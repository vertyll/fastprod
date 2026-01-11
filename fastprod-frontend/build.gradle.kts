import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.vaadin)
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.nullaway)
}

group = "com.vertyll.fastprod"
version = "1.0-SNAPSHOT"
description = "Production management system - front-end"

extra["author"] = "Mikołaj Gawron"
extra["email"] = "gawrmiko@gmail.com"

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
        mavenBom(libs.spring.boot.dependencies.get().toString())
        mavenBom(libs.vaadin.bom.get().toString())
    }
}

dependencies {
    // Implementation
    implementation(libs.bundles.vaadin)
    implementation(libs.spring.boot.starter.web)

    // Compile Only
    compileOnly(libs.lombok)
    compileOnly(libs.jspecify)

    // Annotation Processor
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.guava.beta.checker)

    // Error Prone
    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)

    // Test Implementation
    testImplementation(libs.spring.boot.starter.test)

    // Test Runtime Only
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")

    options.errorprone {
        isEnabled.set(true)

        check("NullAway", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:CustomContractAnnotations", "org.springframework.lang.Contract")
        option("NullAway:JSpecifyMode", "true")

        option("NullAway:ExcludedFieldAnnotations", "lombok.Generated")
        option("NullAway:TreatGeneratedAsUnannotated", "true")

        option("NullAway:AcknowledgeRestrictiveAnnotations", "true")
        option("NullAway:CheckOptionalEmptiness", "true")
        option("NullAway:HandleTestAssertionLibraries", "true")

        excludedPaths.set(".*/build/generated/.*")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        targetExclude("**/build/generated/**/*.java", "**/*Impl.java")

        googleJavaFormat(rootProject.libs.versions.google.java.format.get()).aosp()

        removeUnusedImports()
        importOrder("java", "javax", "org", "com", "lombok", "com.vertyll")

        trimTrailingWhitespace()
        endWithNewline()

        toggleOffOn()
    }

    format("gradle") {
        target("*.gradle.kts", "**/*.gradle.kts")
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
}

tasks.named("bootRun").configure {
    dependsOn("vaadinPrepareFrontend")
}

tasks.named("build").configure {
    dependsOn("vaadinBuildFrontend")
}

tasks.named("spotlessGradle").configure {
    mustRunAfter("vaadinPrepareFrontend")
}
