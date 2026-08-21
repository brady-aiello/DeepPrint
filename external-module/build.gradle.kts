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
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js {
        browser()
        nodejs()
    }
    macosArm64()
    macosX64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    mingwX64()
    linuxX64()
    linuxArm64()
}
