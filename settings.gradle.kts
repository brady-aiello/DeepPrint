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

include(":annotations")
include(":processor")
include(":main-project")
include(":external-module")
