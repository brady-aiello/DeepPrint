plugins {
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
    id("com.bradyaiello.deepprint")
}

val deepPrintVersion = providers.gradleProperty("deepPrintVersion").get()

repositories {
    mavenCentral()
}

kotlin.sourceSets {
    main { kotlin.srcDirs(layout.buildDirectory.dir("generated/ksp/main/kotlin")) }
}

deepPrint {
    overrideToString.set(true)
}

dependencies {
    implementation("com.bradyaiello.deepprint:deep-print-annotations:$deepPrintVersion")
    ksp("com.bradyaiello.deepprint:deep-print-processor:$deepPrintVersion")
    // Pulled in purely as a binary dependency to print a data class out of a jar.
    implementation("com.bradyaiello.deepprint:deep-print-reflection:$deepPrintVersion")
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
