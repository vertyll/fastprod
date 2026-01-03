import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.1.0" apply false
    id("net.ltgt.errorprone") version "4.3.0" apply false
    java
}

group = "com.vertyll"
version = "0.0.1-SNAPSHOT"
description = "Production management system - API"

extra["author"] = "Mikołaj Gawron"
extra["email"] = "gawrmiko@gmail.com"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "net.ltgt.errorprone")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    dependencies {
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        // Error Prone and NullAway
        "errorprone"("com.google.errorprone:error_prone_core:2.36.0")
        "errorprone"("com.uber.nullaway:nullaway:0.12.14")

        compileOnly("org.jspecify:jspecify:1.0.0")
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

        jvmArgs(
            "-XX:+EnableDynamicAgentLoading", "-Xshare:off"
        )
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/**/*.java")

            googleJavaFormat("1.33.0").aosp().reflowLongStrings()

            removeUnusedImports()

            endWithNewline()
            trimTrailingWhitespace()
        }

        kotlin {
            target("**/*.gradle.kts")
            ktlint("1.8.0")
        }
    }
}

tasks.register<TestReport>("testReport") {
    group = "verification"
    description = "Generate aggregated test report for all modules"
    destinationDirectory.set(layout.buildDirectory.dir("reports/all-tests"))

    testResults.from(subprojects.map { it.tasks.withType<Test>() })

    doLast {
        val reportFile = destinationDirectory.get().file("index.html").asFile
        println("\nAggregated test report generated:")
        println("   file://${reportFile.absolutePath}\n")

        val os = System.getProperty("os.name").lowercase()
        try {
            when {
                os.contains("mac") -> {
                    Runtime.getRuntime().exec(arrayOf("open", reportFile.absolutePath))
                }

                os.contains("nix") || os.contains("nux") -> {
                    Runtime.getRuntime().exec(arrayOf("xdg-open", reportFile.absolutePath))
                }

                os.contains("win") -> {
                    Runtime.getRuntime().exec(arrayOf("cmd", "/c", "start", reportFile.absolutePath))
                }
            }
            println("Opening report in browser...\n")
        } catch (e: Exception) {
            println("Could not open browser automatically: ${e.message}\n")
        }
    }
}

tasks.register("testAll") {
    group = "verification"
    description = "Run all tests in all modules and generate aggregated report"

    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    finalizedBy("testReport")
}