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
    // Only Legacy working for now
    js(LEGACY) {
        browser()
        nodejs()
    }
    macosArm64()
    macosX64()
    //watchos()
    //mingwX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":annotations"))
                implementation(project(":external-module"))
            }
            kotlin.srcDir("$buildDir/generated/ksp/metadata/commonMain/kotlin")

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// https://github.com/evant/kotlin-inject/issues/193#issuecomment-1112930931
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}

ksp {
    arg("indent", "2")
}

dependencies {
    add("kspCommonMainMetadata", project(":deep-print-processor"))
}
