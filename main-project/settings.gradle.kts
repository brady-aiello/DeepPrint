pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.50.1"
        id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

include(":annotations")
include(":external-module")
include(":processor")