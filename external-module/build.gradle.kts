repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
}

group = "com.module.external"
version = "unspecified"


kotlin {
    jvm()
}