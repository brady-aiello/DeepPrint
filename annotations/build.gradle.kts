repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
}

kotlin{
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    // Only Legacy working for now
    js(LEGACY) {
        browser()
        nodejs()
    }
    macosArm64()
    macosX64()
    //watchos()
    //mingwX64()
}

group = "com.bradyaiello.deepprint"
version = "0.1.0"