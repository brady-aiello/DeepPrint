pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.50.1"
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

rootProject.name = "deep-print"

include(
    ":deep-print-annotations",
    ":deep-print-processor",
    ":test-project",
    ":test-project-multiplatform",
    ":external-module",
)

includeBuild("convention-plugins")
