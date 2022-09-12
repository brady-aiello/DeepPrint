plugins {
    kotlin("jvm")
}

// Versions are declared in 'gradle.properties' file
val kspVersion: String by project

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(Testing.Junit.jupiter)
}