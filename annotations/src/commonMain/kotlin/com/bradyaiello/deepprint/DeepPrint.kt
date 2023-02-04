package com.bradyaiello.deepprint

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


