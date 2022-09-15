repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
}

kotlin{
    jvm()
}