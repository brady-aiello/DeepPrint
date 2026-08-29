plugins {
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
    id("com.bradyaiello.deepprint")
}

val deepPrintVersion: String by project

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
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
