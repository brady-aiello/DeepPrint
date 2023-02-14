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
    ios()
    iosSimulatorArm64()
    // Only Legacy working with KSP for now
    js(LEGACY) {
        browser()
        nodejs()
    }
    macosArm64()
    macosX64()
    watchos()
    mingwX64()
    linuxX64()
    linuxArm64()
}