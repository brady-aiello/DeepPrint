import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

kotlin.sourceSets {
    main {
        kotlin.srcDirs(layout.buildDirectory.dir("generated/ksp/main/kotlin"))
    }
}

// Spike wiring. A real setup would ship a KotlinCompilerPluginSupportPlugin so consumers
// never see this; passing -Xplugin by hand is enough to find out whether the IR
// transformation works.
val deepPrintCompilerPlugin: Configuration by configurations.creating {
    isTransitive = false
}

dependencies {
    implementation(project(":deep-print-annotations"))
    ksp(project(":deep-print-processor"))
    deepPrintCompilerPlugin(project(":deep-print-compiler-plugin"))
    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile>().configureEach {
    // freeCompilerArgs is not the channel for the plugin jar: the compile task has its
    // own pluginClasspath, which is what KotlinCompilerPluginSupportPlugin feeds in a
    // real setup. Options do go through freeCompilerArgs, in -P plugin:id:key=value form.
    pluginClasspath.from(deepPrintCompilerPlugin)
    compilerOptions.freeCompilerArgs.addAll(
        "-P", "plugin:com.bradyaiello.deepprint:overrideToString=true",
    )
}
