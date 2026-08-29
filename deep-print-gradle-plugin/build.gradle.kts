import de.fayard.refreshVersions.core.versionFor

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("deepprint.publication")
}

val kotlinVersion = versionFor("version.kotlin")

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinVersion")
}

gradlePlugin {
    plugins {
        create("deepPrint") {
            id = "com.bradyaiello.deepprint"
            displayName = "DeepPrint"
            description = "Overrides toString() on data classes with their deepPrint()"
            implementationClass = "com.bradyaiello.deepprint.gradle.DeepPrintGradlePlugin"
        }
    }
}

group = "com.bradyaiello.deepprint"
version = providers.gradleProperty("version").get()

/*
 * The plugin resolves the compiler plugin by Maven coordinate, so its version has to be
 * the version being built. Hardcoding it means that after a bump the Gradle plugin
 * quietly asks for the previous compiler plugin, which resolves and misbehaves rather
 * than failing.
 */
val generateVersionSource = tasks.register("generateVersionSource") {
    val outputDirectory = layout.buildDirectory.dir("generated/version")
    val projectVersion = version.toString()
    inputs.property("version", projectVersion)
    outputs.dir(outputDirectory)
    doLast {
        val file = outputDirectory.get()
            .file("com/bradyaiello/deepprint/gradle/DeepPrintVersion.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.bradyaiello.deepprint.gradle

            internal const val DEEP_PRINT_VERSION: String = "$projectVersion"
            """.trimIndent() + "\n"
        )
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateVersionSource)
}
