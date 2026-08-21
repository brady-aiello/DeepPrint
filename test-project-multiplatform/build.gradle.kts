repositories {
    google()
    mavenCentral()
    mavenLocal()
}

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

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
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":deep-print-annotations"))
                implementation(project(":external-module"))
            }
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// The processor runs once over commonMain and the result is shared with every target,
// so each per-target compile and KSP task has to wait for it.
// https://github.com/evant/kotlin-inject/issues/193#issuecomment-1112930931
tasks.configureEach {
    if (name != "kspCommonMainKotlinMetadata" &&
        (name.startsWith("compile") || name.startsWith("ksp"))
    ) {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

ksp {
    arg("indent", "2")
}

dependencies {
    add("kspCommonMainMetadata", project(":deep-print-processor"))
}
