import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.1.0" apply false
    id("net.ltgt.errorprone") version "4.3.0" apply false
    id("net.ltgt.nullaway") version "2.3.0" apply false
    java
    pmd
}

group = "com.vertyll"
version = "0.0.1-SNAPSHOT"
description = "Production management system - API"

extra["author"] = "Mikołaj Gawron"
extra["email"] = "gawrmiko@gmail.com"

// Shared dependency versions
val errorProneVersion = "2.36.0"
val nullawayVersion = "0.12.14"
val jspecifyVersion = "1.0.0"
val googleJavaFormatVersion = "1.33.0"
val ktlintVersion = "1.8.0"
val betaCheckerVersion = "1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "pmd")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "net.ltgt.errorprone")
    apply(plugin = "net.ltgt.nullaway")

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
        // Test runtime-only dependencies
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        // Error Prone dependencies
        add("errorprone", "com.google.errorprone:error_prone_core:$errorProneVersion")
        add("errorprone", "com.uber.nullaway:nullaway:$nullawayVersion")

        // Annotation processor dependencies
        annotationProcessor("com.google.guava:guava-beta-checker:$betaCheckerVersion")

        // Compile-only dependencies
        compileOnly("org.jspecify:jspecify:$jspecifyVersion")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")

        options.errorprone {
            isEnabled.set(true)

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

        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
            showStandardStreams = false
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            displayGranularity = 2
            
            showStandardStreams = false
            showCauses = true
            showStackTraces = true
        }

        val ANSI_RESET = "\u001B[0m"
        val ANSI_GREEN = "\u001B[32m"
        val ANSI_RED = "\u001B[31m"
        val ANSI_YELLOW = "\u001B[33m"
        val ANSI_BLUE = "\u001B[34m"
        val ANSI_CYAN = "\u001B[36m"
        val ANSI_BOLD = "\u001B[1m"
        
        val CHECK_MARK = "✓"
        val CROSS_MARK = "✗"
        val SKIP_MARK = "⊘"

        afterTest(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            val indicator = when (result.resultType) {
                TestResult.ResultType.SUCCESS -> "$ANSI_GREEN$CHECK_MARK$ANSI_RESET"
                TestResult.ResultType.FAILURE -> "$ANSI_RED$CROSS_MARK$ANSI_RESET"
                TestResult.ResultType.SKIPPED -> "$ANSI_YELLOW$SKIP_MARK$ANSI_RESET"
                else -> "?"
            }
            val duration = result.endTime - result.startTime
            println("  $indicator ${desc.className} > ${desc.name} ${ANSI_CYAN}(${duration}ms)$ANSI_RESET")
        }))

        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) {
                val total = result.testCount
                val passed = result.successfulTestCount
                val failed = result.failedTestCount
                val skipped = result.skippedTestCount
                val duration = result.endTime - result.startTime
                
                println()
                println("$ANSI_BOLD═══════════════════════════════════════════════════════════════$ANSI_RESET")
                println("$ANSI_BOLD                        TEST RESULTS                        $ANSI_RESET")
                println("$ANSI_BOLD═══════════════════════════════════════════════════════════════$ANSI_RESET")
                println()
                println("  Total:   $ANSI_BOLD$total$ANSI_RESET tests")
                println("  Passed:  $ANSI_GREEN$ANSI_BOLD$passed$ANSI_RESET $ANSI_GREEN$CHECK_MARK$ANSI_RESET")
                println("  Failed:  $ANSI_RED$ANSI_BOLD$failed$ANSI_RESET ${if (failed > 0) "$ANSI_RED$CROSS_MARK$ANSI_RESET" else ""}")
                println("  Skipped: $ANSI_YELLOW$ANSI_BOLD$skipped$ANSI_RESET ${if (skipped > 0) "$ANSI_YELLOW$SKIP_MARK$ANSI_RESET" else ""}")
                println()
                println("  Duration: $ANSI_CYAN${duration}ms$ANSI_RESET")
                println()
                
                val statusColor = when (result.resultType) {
                    TestResult.ResultType.SUCCESS -> ANSI_GREEN
                    TestResult.ResultType.FAILURE -> ANSI_RED
                    else -> ANSI_YELLOW
                }
                println("  Status: $statusColor$ANSI_BOLD${result.resultType}$ANSI_RESET")
                println()
                println("$ANSI_BOLD═══════════════════════════════════════════════════════════════$ANSI_RESET")
                println()
            }
        }))
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

    pmd {
        isConsoleOutput = true
        toolVersion = "7.20.0"
        ruleSets = listOf()
        ruleSetFiles = files(rootProject.file("config/pmd/pmd-main-ruleset.xml"))
        isIgnoreFailures = false
    }

    tasks.withType<Pmd> {
        if (name == "pmdTest") {
            ruleSetFiles = files(rootProject.file("config/pmd/pmd-test-ruleset.xml"))
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