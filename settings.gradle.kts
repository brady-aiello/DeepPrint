pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.60.6"
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

rootProject.name = "deep-print"

include(
    ":deep-print-annotations",
    ":deep-print-processor",
    ":deep-print-compiler-plugin",
    ":deep-print-reflection",
    ":test-project",
    ":test-project-multiplatform",
    ":test-project-tostring",
    ":external-module",
)

includeBuild("convention-plugins")
