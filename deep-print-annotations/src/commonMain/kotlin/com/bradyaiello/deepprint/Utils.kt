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

fun String.deepPrint(): String = "\"$this\""

fun Byte.deepPrint(): String = toString()

fun Short.deepPrint(): String = toString()

fun Int.deepPrint(): String = toString()

fun Long.deepPrint(): String = toString()

fun Double.deepPrint(): String = formatForJS()

fun Boolean.deepPrint(): String = toString()

fun Char.deepPrint(): String = "'${this}'"

fun Float.deepPrint(): String = "${this.formatForJS()}f"

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

fun <T> List<T>.deepPrintContents(): String {
    val stringBuilder = StringBuilder()
    this.forEach { value ->
        val toAdd = deepPrintPrimitive(value)
        stringBuilder.append(" $toAdd,")
    }
    return stringBuilder.toString()
}

fun <T> Array<T>.deepPrintContents(): String = this.toList().deepPrintContents()

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
