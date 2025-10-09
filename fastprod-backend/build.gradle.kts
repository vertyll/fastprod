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
            "-XX:+EnableDynamicAgentLoading",
            "-Xshare:off"
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

