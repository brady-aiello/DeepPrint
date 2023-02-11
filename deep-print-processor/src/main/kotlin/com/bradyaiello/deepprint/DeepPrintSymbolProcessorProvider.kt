package com.bradyaiello.deepprint

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DeepPrintSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val indent = (environment.options["indent"] ?: "4").toInt()
        return DeepPrintProcessor(
            codeGenerator = environment.codeGenerator,
            indent = indent,
        )
    }
}