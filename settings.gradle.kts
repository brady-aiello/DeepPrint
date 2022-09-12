pluginManagement {
    // Versions are declared in 'gradle.properties' file
    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
        id("de.fayard.refreshVersions") version "0.50.0"
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "deep-print"

include(":annotations")
include(":processor")
include(":main-project")
