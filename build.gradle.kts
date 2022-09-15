buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    id("com.google.devtools.ksp") apply false
    id("io.gitlab.arturbosch.detekt") apply false
}

repositories {
    google()
    mavenCentral()
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}