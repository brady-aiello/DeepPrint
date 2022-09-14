package com.bradyaiello.deepprint

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DeepPrint()

/*
TODO(Custom Deep Printer feature?)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DeepPrintWith(val deepPrinter: KClass<out DeepPrinter<*>>)

interface DeepPrinter<T> {
    fun deepPrintWith(data: T): String
}

data class Something(val id: String, val number: Int)

class SomethingDeepPrinter: DeepPrinter<Something> {
    override fun deepPrintWith(data: Something): String {
        return ""
    }
}

data class SomeTestClass(
    val name: String,
    @DeepPrintWith(deepPrinter = SomethingDeepPrinter::class)
    val something: Something
)
*/

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


