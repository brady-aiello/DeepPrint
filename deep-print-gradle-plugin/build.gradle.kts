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
