repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvm()
    js { nodejs() }
    macosArm64()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":deep-print-annotations"))
            }
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.configureEach {
    if (name != "kspCommonMainKotlinMetadata" &&
        (name.startsWith("compile") || name.startsWith("ksp"))
    ) {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":deep-print-processor"))

    // Every compilation gets its own plugin classpath, and Kotlin/Native has a separate
    // one again. A real consumer never sees this: KotlinCompilerPluginSupportPlugin is
    // what fills these in.
    listOf(
        "kotlinCompilerPluginClasspathJvmMain",
        "kotlinCompilerPluginClasspathJvmTest",
        "kotlinCompilerPluginClasspathJsMain",
        "kotlinCompilerPluginClasspathJsTest",
        "kotlinCompilerPluginClasspathMacosArm64Main",
        "kotlinCompilerPluginClasspathMacosArm64Test",
        "kotlinNativeCompilerPluginClasspath",
    ).forEach { configurationName ->
        add(configurationName, project(":deep-print-compiler-plugin"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.addAll(
        "-P", "plugin:com.bradyaiello.deepprint:overrideToString=true",
    )
}
