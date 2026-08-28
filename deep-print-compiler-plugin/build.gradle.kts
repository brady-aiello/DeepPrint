import de.fayard.refreshVersions.core.versionFor

repositories {
    google()
    mavenCentral()
}

plugins {
    `java-library`
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("deepprint.publication")
}

val kotlinVersion = versionFor("version.kotlin")

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
}

group = "com.bradyaiello.deepprint"
version = providers.gradleProperty("version").get()
