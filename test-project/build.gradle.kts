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

// Allows running from command line using  ./gradlew :test-project:run
application {
    mainClass.set("com.bradyaiello.deepprint.MainKt")
}

kotlin.sourceSets {
    main {
        kotlin.srcDirs(
            file("$buildDir/generated/ksp/main/kotlin"),
        )
    }
    test {
        kotlin.srcDirs(
            file("$buildDir/generated/ksp/test/kotlin"),
        )
    }
}

dependencies {
    implementation(project(":deep-print-annotations"))
    implementation(project(":deep-print-processor"))
    ksp(project(":deep-print-processor"))
    implementation(project(":external-module"))
    testImplementation(kotlin("test"))
}