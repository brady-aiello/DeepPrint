repositories {
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

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation(platform(Testing.Junit.bom))
    testImplementation(Testing.Junit.jupiter)
    // Gradle 9 no longer puts the JUnit Platform launcher on the test runtime classpath for us.
    testRuntimeOnly(platform(Testing.Junit.bom))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(project(":deep-print-annotations"))
}

tasks.test {
    useJUnitPlatform()
}

group = "com.bradyaiello.deepprint"
version = providers.gradleProperty("version").get()
