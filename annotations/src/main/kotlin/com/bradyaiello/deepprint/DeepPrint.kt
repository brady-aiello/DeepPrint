package com.bradyaiello.deepprint

@Target(AnnotationTarget.CLASS)
annotation class DeepPrint()

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


