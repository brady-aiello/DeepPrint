import de.fayard.refreshVersions.core.versionFor

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
}

val kspVersion = versionFor("plugin.com.google.devtools.ksp")

dependencies {
    implementation(project(":deep-print-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(Testing.Junit.jupiter)
}

group = "com.bradyaiello.deepprint"
version = "0.1.0"