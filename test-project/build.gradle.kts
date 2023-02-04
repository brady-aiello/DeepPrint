repositories {
    google()
    mavenCentral()
    mavenLocal()
}

plugins {
    kotlin("jvm")
    application
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

// Allows running from command line using  ./gradlew :main-project:run
application {
    mainClass.set("com.bradyaiello.deepprint.MainKt")
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
    )
}

kotlin.sourceSets.test {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/test/kotlin"),
    )
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    val useLocal = true

    implementation(project(":annotations"))
    implementation(project(":external-module"))
    implementation(project(":deep-print-processor"))
    ksp(project(":deep-print-processor"))
//    implementation("io.github.brady-aiello:deep-print-processor:0.1.0")
//    ksp(project("io.github.brady-aiello:deep-print-processor:0.1.0"))
    testImplementation(Testing.Junit.jupiter)
    testImplementation(KotlinX.datetime)
}