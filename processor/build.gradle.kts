plugins {
    kotlin("multiplatform")
}

// Versions are declared in 'gradle.properties' file
val kspVersion: String by project

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(Testing.Junit.jupiter)
            }
        }
    }
}

