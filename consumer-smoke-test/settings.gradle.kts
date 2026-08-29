/*
 * A separate build on purpose. It consumes DeepPrint from Maven Central the way a
 * stranger would, so it must not see this repository's projects or its mavenLocal.
 */
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    val deepPrintVersion = providers.gradleProperty("deepPrintVersion").get()
    plugins {
        id("com.bradyaiello.deepprint") version deepPrintVersion
    }
}

rootProject.name = "consumer-smoke-test"
