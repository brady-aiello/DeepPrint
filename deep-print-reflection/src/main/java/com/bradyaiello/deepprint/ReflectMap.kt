package com.bradyaiello.deepprint


fun <K, V> MutableMap<K, V>.deepPrintMutableMapReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "mutableMapOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig) {
        deepPrintMutableMapReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <K, V> MutableMap<K, V>.deepPrintMutableMapReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "mutableMapOf",
    standalone: Boolean = true,
): String {
    return deepPrintMapReflection(
        startingIndent,
        indentSize,
        constructor,
        standalone,
    )
}

fun <K, V> Map<K, V>.deepPrintMapReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "mapOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig) {
        deepPrintMapReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <K, V> Map<K, V>.deepPrintMapReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "mapOf",
    standalone: Boolean = true,
): String {
    val stringBuilder = StringBuilder()
    val start = startingIndent.indent()
    val prefix = if (standalone) start else " "
    stringBuilder.append("${prefix}$constructor(\n")

    this.forEach { entry ->
        entry.deepPrintMapEntryReflection(
            stringBuilder = stringBuilder,
            startingIndent = startingIndent,
            indentSize = indentSize,
        )
    }
    stringBuilder.append("${start})")
    return stringBuilder.toString()
}

private fun <Any> Any?.deepPrintMapKeyOrValue(
    stringBuilder: StringBuilder,
    startingIndent: Int,
    indentSize: Int
) {
    val totalIndent = startingIndent.indent() + indentSize.indent()

    if (this == null) {
        stringBuilder.append("${totalIndent}null")
    } else if (this!!::class.isPrimitive()) {
        stringBuilder.append("${totalIndent}${deepPrintPrimitive(this)}")
    } else {
        stringBuilder.append(
            this.deepPrintReflection(
                initialIndentLength = startingIndent + indentSize,
                indentIncrementLength = indentSize,
            )
        )
    }
}

private fun <K, V> Map.Entry<K, V?>.deepPrintMapEntryReflection(
    stringBuilder: StringBuilder,
    startingIndent: Int,
    indentSize: Int,
) {
    key.deepPrintMapKeyOrValue(
        stringBuilder = stringBuilder,
        startingIndent = startingIndent,
        indentSize = indentSize
    )
    val singleLine = key!!.isPrimitive() && ((value == null) || value!!.isPrimitive())
    val toStr = if (singleLine) " to " else " to\n"
    stringBuilder.append(toStr)
    val newStartingIndent = if (singleLine) 0 else startingIndent + indentSize
    val newIndentSize = if (singleLine) 0 else indentSize
    value.deepPrintMapKeyOrValue(
        stringBuilder = stringBuilder,
        startingIndent = newStartingIndent,
        indentSize = newIndentSize
    )
    val suffix = if (singleLine) ",\n" else "\n"
    stringBuilder.append(suffix)
}
