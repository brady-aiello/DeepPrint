package com.bradyaiello.deepprint.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

abstract class DeepPrintExtension {
    /**
     * Replace the synthesised toString() of each data class with its deepPrint().
     *
     * Off by default: this changes every log line, string template and debugger view in
     * the module, which is not something to switch on without meaning to.
     */
    abstract val overrideToString: Property<Boolean>
}

/**
 * Wires the DeepPrint compiler plugin into every Kotlin compilation, including the
 * Native and JS ones, each of which takes its plugin classpath from a different place.
 */
class DeepPrintGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("deepPrint", DeepPrintExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = "com.bradyaiello.deepprint"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.bradyaiello.deepprint",
        artifactId = "deep-print-compiler-plugin",
        version = DEEP_PRINT_VERSION,
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(DeepPrintExtension::class.java)
        return project.provider {
            listOf(
                SubpluginOption(
                    key = "overrideToString",
                    value = extension.overrideToString.getOrElse(false).toString(),
                )
            )
        }
    }
}
