import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    pmd
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.vaadin)
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.nullaway)
    alias(libs.plugins.spotbugs)
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
    implementation(libs.spring.boot.starter.actuator)

    // Compile Only
    compileOnly(libs.lombok)
    compileOnly(libs.jspecify)
    compileOnly(libs.spotbugs.annotations)

    // Annotation Processor
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.guava.beta.checker)

    // Error Prone
    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)

    // Spotbugs
    spotbugsPlugins(libs.findsecbugs)

    // Test Implementation
    testImplementation(libs.spring.boot.starter.test)

    // Test Compile Only
    testCompileOnly(libs.jspecify)
    testCompileOnly(libs.spotbugs.annotations)

    // Test Runtime Only
    testRuntimeOnly(libs.junit.platform.launcher)
}

configure<com.github.spotbugs.snom.SpotBugsExtension> {
    ignoreFailures.set(false)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
    showProgress.set(true)

    excludeFilter.set(file("config/spotbugs/exclude-filter.xml"))
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    val projectName = project.name
    val taskName = name
    val buildDir = project.layout.buildDirectory.get()

    reports.maybeCreate("html").apply {
        required.set(true)
        outputLocation.set(file("${buildDir}/reports/spotbugs/${projectName}-${taskName}.html"))
        setStylesheet("fancy-hist.xsl")
    }

    reports.maybeCreate("xml").apply {
        required.set(true)
        outputLocation.set(file("${buildDir}/reports/spotbugs/${projectName}-${taskName}.xml"))
    }

    doLast {
        val ansiReset = "\u001B[0m"
        val ansiGreen = "\u001B[32m"
        val ansiRed = "\u001B[31m"
        val ansiBold = "\u001B[1m"

        val xmlReport = reports.maybeCreate("xml").outputLocation.get().asFile
        val htmlReport = reports.maybeCreate("html").outputLocation.get().asFile

        if (xmlReport.exists()) {
            val xml = xmlReport.readText()
            val bugCount = xml.substringAfter("<BugInstance", "").let {
                if (it.isEmpty()) 0 else xml.split("<BugInstance").size - 1
            }

            println("\n$ansiBold═══════════════════════════════════════════════════════════════$ansiReset")
            println("$ansiBold  SpotBugs: $projectName - $taskName")
            println("$ansiBold═══════════════════════════════════════════════════════════════$ansiReset")

            if (bugCount == 0) {
                println("  ${ansiGreen}✓ No bugs found!$ansiReset")
            } else {
                println("  ${ansiRed}✗ Found $bugCount bug(s)$ansiReset")

                val categories = mutableMapOf<String, Int>()
                xml.split("<BugInstance").drop(1).forEach { bugXml ->
                    val category = bugXml.substringAfter("category=\"", "").substringBefore("\"", "UNKNOWN")
                    categories[category] = categories.getOrDefault(category, 0) + 1
                }

                println("\n  Categories:")
                categories.forEach { (category, count) ->
                    println("    • $category: $count")
                }

                if (htmlReport.exists()) {
                    println("\n  Detailed report: file://${htmlReport.absolutePath}")
                }
            }
            println("$ansiBold═══════════════════════════════════════════════════════════════$ansiReset\n")
        }
    }
}

tasks.named("check") {
    dependsOn("spotbugsMain")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")

    options.errorprone {
        enabled.set(true)

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

        googleJavaFormat(libs.versions.google.java.format.get()).aosp()

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

pmd {
    isConsoleOutput = true
    toolVersion = libs.versions.pmd.get()
    ruleSets = listOf()
    ruleSetFiles = files(file("config/pmd/pmd-main-ruleset.xml"))
    isIgnoreFailures = false
}

tasks.withType<Pmd> {
    if (name == "pmdTest") {
        ruleSetFiles = files(file("config/pmd/pmd-test-ruleset.xml"))
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
