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
    implementation(project(":deep-print-annotations"))
}

tasks.test {
    useJUnitPlatform()
}

group = "com.bradyaiello.deepprint"
version = properties["version"]!!
