import de.fayard.refreshVersions.core.versionFor

plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
}

val kspVersion = versionFor("plugin.com.google.devtools.ksp")

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(Testing.Junit.jupiter)
            }
        }
    }
}

group = "io.github.brady-aiello"
version = "0.1.0"