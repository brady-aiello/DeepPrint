repositories {
    mavenCentral()
}

plugins {
    `java-library`
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("deepprint.publication")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation(platform(Testing.Junit.bom))
    testImplementation(Testing.Junit.jupiter)
    // Gradle 9 no longer puts the JUnit Platform launcher on the test runtime classpath for us.
    testRuntimeOnly(platform(Testing.Junit.bom))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(project(":deep-print-annotations"))
    // Only to prove a data class from another module reflects the same as a local one.
    testImplementation(project(":external-module"))
}

// The other half of the runtime API consumers compile against.
kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
    }
}

tasks.test {
    useJUnitPlatform()
}

group = "com.bradyaiello.deepprint"
version = providers.gradleProperty("version").get()
