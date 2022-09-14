plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    application
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
        file("$buildDir/generated/ksp/test"),
    )
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
    testImplementation(Testing.Junit.jupiter)
    testImplementation(KotlinX.datetime)
}