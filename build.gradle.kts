plugins {
    kotlin("multiplatform") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.5"
}

group = "me.npanuhin"
version = "edu_kotlin-js_todo_list"

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser {
            distribution {
                outputDirectory.set(projectDir.resolve("output"))
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                // React, React DOM + Wrappers
                implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.430"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

                // Kotlin React Emotion (CSS)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            }
        }
    }
}

// Code linting:
detekt {
    ignoreFailures = true
    buildUponDefaultConfig = false
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("customDetekt") {
    description = "Runs detekt"
    setSource(files("src/main/kotlin", "src/test/kotlin"))
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt.yml"))
    debug = true
    ignoreFailures = false
    reports {
        html.outputLocation.set(file("build/reports/detekt.html"))
    }
    include("**/*.kt")
    include("**/*.kts")
    exclude("resources/")
    exclude("build/")
}

diktat {
    reporter = "html"
    output = "build/reports/diktat.html"

    diktatConfigFile = file("$projectDir/config/diktat.yml")
    inputs {
        include("**/*.kts")
        include("**/*.kt")
    }
}
