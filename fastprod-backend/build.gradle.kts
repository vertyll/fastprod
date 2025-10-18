plugins {
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    java
}

group = "com.vertyll"
version = "0.0.1-SNAPSHOT"
description = "Production management system - api"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    dependencies {
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
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

            googleJavaFormat("1.22.0").aosp().reflowLongStrings()

            removeUnusedImports()

            endWithNewline()
            trimTrailingWhitespace()
        }

        kotlin {
            target("**/*.gradle.kts")
            ktlint()
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

tasks.register("buildFast") {
    group = "build"
    description = "Fast build without tests, formatting, and documentation"
    dependsOn(tasks.named("classes"))

    tasks.withType<Test>().configureEach {
        enabled = false
    }

    doFirst {
        println("Fast build - skipping tests, formatting, and docs...")
    }
}
