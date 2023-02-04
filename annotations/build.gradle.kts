repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
}

kotlin{
    jvm()
    ios()
    js(IR) {
        browser()
        nodejs()
    }
}

group = "com.bradyaiello.deepprint"
version = "0.1.0"