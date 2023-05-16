package com.bradyaiello.deepprint

fun <T> Array<T>.deepPrintArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "arrayOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig) {
        deepPrintArrayReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <T> Array<T>.deepPrintArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "arrayOf",
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
