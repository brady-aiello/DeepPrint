repositories {
    google()
    mavenCentral()
    mavenLocal()
}

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

kotlin.sourceSets {
    test {
        kotlin.srcDirs(
            layout.buildDirectory.dir("generated/ksp/test/kotlin"),
        )
    }
}

// The point of this module: no @DeepPrint anywhere in its sources.
ksp {
    arg("processAllDataClasses", "true")
}

dependencies {
    implementation(project(":deep-print-annotations"))
    kspTest(project(":deep-print-processor"))
    testImplementation(project(":deep-print-annotations"))
    testImplementation(kotlin("test"))
}
