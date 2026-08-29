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

val kspVersion = versionFor("plugin.com.google.devtools.ksp")
val kotlinPoetVersion = versionFor("version.kotlinpoet")

dependencies {
    implementation(project(":deep-print-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    // Models packages, imports, visibility and type names as objects rather than text.
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    testImplementation(platform(Testing.Junit.bom))
    testImplementation(Testing.Junit.jupiter)
    // Gradle 9 no longer puts the JUnit Platform launcher on the test runtime classpath for us.
    testRuntimeOnly(platform(Testing.Junit.bom))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = "com.bradyaiello.deepprint"
version = providers.gradleProperty("version").get()

