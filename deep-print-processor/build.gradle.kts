import de.fayard.refreshVersions.core.versionFor

repositories {
    google()
    mavenCentral()
}

plugins {
    `java-library`
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("jvm.convention.publication")
}

java {
    withJavadocJar()
    withSourcesJar()
}

val kspVersion = versionFor("plugin.com.google.devtools.ksp")

dependencies {
    implementation(project(":deep-print-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(Testing.Junit.jupiter)
}

group = "com.bradyaiello.deepprint"
version = properties["version"]!!

