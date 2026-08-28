package com.bradyaiello.deepprint.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal val OVERRIDE_TO_STRING = CompilerConfigurationKey<Boolean>("overrideToString")

/** Receives the options the Gradle plugin passes through to the compiler. */
@OptIn(ExperimentalCompilerApi::class)
class DeepPrintCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "com.bradyaiello.deepprint"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = "overrideToString",
            valueDescription = "true|false",
            description = "Replace the synthesised toString() of each data class with its deepPrint()",
            required = false,
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            "overrideToString" -> configuration.put(OVERRIDE_TO_STRING, value.toBoolean())
            else -> error("Unknown DeepPrint plugin option: ${option.optionName}")
        }
    }
}
