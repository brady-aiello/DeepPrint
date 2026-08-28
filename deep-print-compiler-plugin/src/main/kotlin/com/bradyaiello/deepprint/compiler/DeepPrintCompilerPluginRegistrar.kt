package com.bradyaiello.deepprint.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class DeepPrintCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = "com.bradyaiello.deepprint"

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // Off unless asked for: replacing toString() across a whole module is not
        // something to do by default.
        if (configuration.get(OVERRIDE_TO_STRING) == true) {
            IrGenerationExtension.registerExtension(DeepPrintIrGenerationExtension())
        }
    }
}
