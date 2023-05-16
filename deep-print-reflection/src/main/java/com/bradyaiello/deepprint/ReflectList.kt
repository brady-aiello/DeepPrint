package com.bradyaiello.deepprint

fun <T> List<T>.deepPrintListReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "listOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig){
        deepPrintListReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <T> List<T>.deepPrintListReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "listOf",
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

fun <T> MutableList<T>.deepPrintMutableListReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "mutableListOf",
        standalone = true,
    )
): String {
    return with(deepPrintReflectConfig) {
        deepPrintMutableListReflection(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    }
}

fun <T> MutableList<T>.deepPrintMutableListReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "mutableListOf",
    standalone: Boolean = true,
): String {
    return this.deepPrintListReflection(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
}

internal fun <Any> Any?.deepPrintListItem(
    stringBuilder: StringBuilder,
    startingIndent: Int,
    indentSize: Int
) {
    val totalIndent = startingIndent.indent() + indentSize.indent()

    if (this == null) {
        stringBuilder.append("${totalIndent}null,\n")
    } else if (this!!::class.isPrimitive()) {
        stringBuilder.append("${totalIndent}${deepPrintPrimitive(this)},\n")
    } else {
        stringBuilder.append(
            this.deepPrintReflection(
                initialIndentLength = startingIndent + indentSize,
                indentIncrementLength = indentSize,
            ) + "\n"
        )
    }
}

