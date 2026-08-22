package com.bradyaiello.deepprint

fun <T> Set<T>.deepPrintSetReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "setOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig) {
        deepPrintSetReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <T> Set<T>.deepPrintSetReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "setOf",
    standalone: Boolean = true,
): String {
    val stringBuilder = StringBuilder()
    val start = startingIndent.indent()
    val prefix = if (standalone) start else " "
    stringBuilder.append("${prefix}$constructor(\n")
    this.forEach { value ->
        value.deepPrintListItem(stringBuilder, startingIndent, indentSize)
    }
    stringBuilder.append("${start})")
    return stringBuilder.toString()
}

fun <T> MutableSet<T>.deepPrintMutableSetReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "mutableSetOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig) {
        deepPrintMutableSetReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <T> MutableSet<T>.deepPrintMutableSetReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "mutableSetOf",
    standalone: Boolean = true,
): String {
    return this.deepPrintSetReflection(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
}
