pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.51.0"
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

rootProject.name = "deep-print"

include(
    ":deep-print-annotations",
    ":deep-print-processor",
    ":deep-print-reflection",
    ":test-project",
    ":test-project-multiplatform",
    ":external-module",
)

includeBuild("convention-plugins")
