package com.bradyaiello.deepprint

import kotlin.math.floor


fun <T>deepPrintPrimitive(value: T): String {
    return when (value) {
        is String -> "\"$value\""
        is Byte,
        is Short,
        is Int,
        is Long,
        is Double,
        is Boolean -> "$value"
        is Char -> "'${value}'"
        is Float -> "${value.formatForJS()}f"
        else -> "$value"
    }
}

/*
 * These take a nullable receiver so that generated code does not have to special-case
 * a nullable property: `val name: String?` and `val name: String` both come out of the
 * processor as `${name.deepPrint()}`, and a null prints as the `null` literal.
 */

fun String?.deepPrint(indent: Int = 0): String =
    if (this == null) "${indent.indent()}null" else "${indent.indent()}\"$this\""

fun Byte?.deepPrint(indent: Int = 0): String = "${indent.indent()}${this ?: "null"}"

fun Short?.deepPrint(indent: Int = 0): String = "${indent.indent()}${this ?: "null"}"

fun Int?.deepPrint(indent: Int = 0): String = "${indent.indent()}${this ?: "null"}"

fun Long?.deepPrint(indent: Int = 0): String = "${indent.indent()}${this ?: "null"}"

fun Double?.deepPrint(indent: Int = 0): String =
    if (this == null) "${indent.indent()}null" else "${indent.indent()}${formatForJS()}"

fun Boolean?.deepPrint(indent: Int = 0): String = "${indent.indent()}${this ?: "null"}"

fun Char?.deepPrint(indent: Int = 0): String =
    if (this == null) "${indent.indent()}null" else "${indent.indent()}'${this}'"

fun Float?.deepPrint(indent: Int = 0): String =
    if (this == null) "${indent.indent()}null" else "${indent.indent()}${formatForJS()}f"

fun Int.indent(): String = " ".repeat(this)

fun <K, V> Map<K, V>.deepPrint(
    keyTransform: (K) -> String,
    valueTransform: (V) -> String,
    indent: Int = 4,
): String {
    val indentSpace = " ".repeat(indent)
    return "mapOf(\n${deepPrintContents({indentSpace + keyTransform(it)},{valueTransform(it)})})"
}

fun <K, V> Map<K, V>.deepPrintContents(
    keyTransform: (K) -> String,
    valueTransform: (V) -> String,
): String {
    val stringBuilder = StringBuilder()
    entries.forEach { (key, value) ->
        val keyString = keyTransform(key)
        val valueString = valueTransform(value)
        stringBuilder.append("$keyString to $valueString,\n")
    }
    return stringBuilder.toString()
}

private inline fun <T> Iterable<T>.deepPrintItems(transform: (T) -> String): String {
    val stringBuilder = StringBuilder()
    this.forEach { value ->
        stringBuilder.append(" ${transform(value)},")
    }
    return stringBuilder.toString()
}

fun <T> List<T>.deepPrintContents(): String = deepPrintItems { deepPrintPrimitive(it) }

fun <T> Set<T>.deepPrintContents(): String = deepPrintItems { deepPrintPrimitive(it) }

fun <T> Array<T>.deepPrintContents(): String = asIterable().deepPrintItems { deepPrintPrimitive(it) }

/*
 * The primitive arrays dispatch on the static element type rather than going through
 * deepPrintPrimitive(). Kotlin/JS compiles Float, Double and Char down to plain numbers,
 * so a runtime `is Float` / `is Char` check there would print 2.0f as `2` and 'A' as `65`.
 */

fun ByteArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun ShortArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun IntArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun LongArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun FloatArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun DoubleArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun BooleanArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

fun CharArray.deepPrintContents(): String = asIterable().deepPrintItems { it.deepPrint() }

/**
 * In jvm, android, and native platforms, printing 2.0f
 * results in `2.0`. For KotlinJS, it prints `2`. This
 * ensures that all platforms print the same, as `2.0f`
 */
fun Float.formatForJS(): String {
    return if (floor(this) == this) {
        this.toInt().toString() + ".0"
    } else {
        this.toString()
    }
}

/**
 * In jvm, android, and native platforms, printing 2.0
 * results in `2.0`. For KotlinJS, it prints `2`. This
 * ensures that all platforms print the same, as `2.0`
 */
fun Double.formatForJS(): String {
    return if (floor(this) == this) {
        this.toInt().toString() + ".0"
    } else {
        this.toString()
    }
}
