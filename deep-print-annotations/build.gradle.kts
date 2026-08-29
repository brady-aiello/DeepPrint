repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
    id("deepprint.publication")
}

kotlin{
    // Every consumer compiles against this module, on every target. Enabling the
    // block is what turns validation on; there is nothing to set inside it.
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
    }
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

group = "com.bradyaiello.deepprint"
version = providers.gradleProperty("version").get()
