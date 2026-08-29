package com.example.consumer

import com.bradyaiello.deepprint.DeepPrint

@DeepPrint
data class Packaged(val name: String, val values: List<Int>)

/**
 * DeepPrintReflectConfig comes from deep-print-reflection, resolved from Maven Central
 * as a jar. There is no source for it anywhere in this build, which is the case a module
 * inside the library's own repository cannot reproduce.
 */
@DeepPrint
data class HoldsBinaryExternal(
    val config: com.bradyaiello.deepprint.DeepPrintReflectConfig,
    val note: String,
)
