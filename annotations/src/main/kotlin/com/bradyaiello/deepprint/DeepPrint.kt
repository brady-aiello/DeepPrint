package com.bradyaiello.deepprint

@Target(AnnotationTarget.CLASS)
annotation class DeepPrint()

fun <T> List<T>.deepPrintContents(indent: Int): String {
    val stringBuilder = StringBuilder()
    val indentString = " ".repeat(indent)
    this.forEachIndexed { index, value ->
        val toAdd = when (value) {
            is String -> "\"\$${value}\", "
            is Byte,
            is Short,
            is Int,
            is Long,
            is Double,
            is Boolean -> "${value}, "
            is Char -> "'${value}', "
            is Float -> "${value}f, "
            else -> {
                value.toString()
            }
        }
        if (index == 0) {
            stringBuilder.append(indentString)
        }
        stringBuilder.append(toAdd)
    }
    return stringBuilder.toString()
}


