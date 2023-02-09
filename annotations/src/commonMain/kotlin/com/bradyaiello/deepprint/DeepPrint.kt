package com.bradyaiello.deepprint

import kotlin.math.floor

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DeepPrint()

fun <T> Array<T>.deepPrintContents(): String = this.toList().deepPrintContents()

fun <T> List<T>.deepPrintContents(): String {
    val stringBuilder = StringBuilder()
    this.forEach { value ->
        val toAdd = when (value) {
            is String -> "\"$value\","
            is Byte,
            is Short,
            is Int,
            is Long,
            is Double,
            is Boolean -> "${value},"
            is Char -> "'${value}',"
            is Float -> "${value}f,"
            else -> {
                value.toString()
            }
        }
        stringBuilder.append(" $toAdd")
    }
    return stringBuilder.toString()
}

/**
 * In jvm, android, and native platforms, printing 2.0f
 * results in `2.0`. For KotlinJS, it prints `2`. To ensure
 * that all platforms print the same, as `2.0`
 */
fun Float.formatForJS(): String {
    return if (floor(this) == this) {
        this.toInt().toString() + ".0"
    } else {
        this.toString()
    }
}

/**
 * In jvm, android, and native platforms, printing 2.0f
 * results in `2.0`. For KotlinJS, it prints `2`. To ensure
 * that all platforms print the same, as `2.0`
 */
fun Double.formatForJS(): String {
    return if (floor(this) == this) {
        this.toInt().toString() + ".0"
    } else {
        this.toString()
    }
}
