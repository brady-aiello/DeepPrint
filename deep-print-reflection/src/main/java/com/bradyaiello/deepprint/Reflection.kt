package com.bradyaiello.deepprint

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

fun Any?.deepPrintReflection(
    initialIndentLength: Int = 0,
    indentIncrementLength: Int = 4,
): String {
    if (this == null || !this::class.isData) {
        return ""
    }
    val kClass = this::class

    val initialIndent = if (initialIndentLength == 0) ""
    else initialIndentLength.indent()

    val indentIncrement = indentIncrementLength.indent()

    val builder = StringBuilder()
    val constructor = kClass.constructors.first()
    val constructorCall = "${kClass.simpleName!!}(\n"
    builder.append("$initialIndent$constructorCall")
    val params = constructor.parameters

    params.forEach { kParam ->
        val propName = kParam.name
        val propValue = this.getPropertyValue(kParam)!!
        if (propValue::class.isPrimitive()) {
            builder.append("$initialIndent$indentIncrement$propName = ${deepPrintPrimitive(propValue)},\n")
        } else if (propValue is List<*>) {
            /*
                List and MutableList look identical at runtime. 
                They both are implemented by Java Arraylist.
                So we must default to using the constructor for mutableListOf(),
                which works for List and MutableList.
                https://youtrack.jetbrains.com/issue/KT-23652/Reflection-Classifier-for-MutableListT-same-as-for-ListT
                https://youtrack.jetbrains.com/issue/KT-11754/Support-special-KClass-instances-for-mutable-collection-interfaces
             */
            builder.append(
                "$initialIndent$indentIncrement$propName = ${
                    propValue.deepPrintListReflection(
                        startingIndent = initialIndentLength + indentIncrementLength,
                        indentSize = indentIncrementLength,
                        standalone = false,
                        constructor = "mutableListOf",
                    )
                },\n"
            )
        } else {
            builder.append(
                propValue.deepPrintReflection(
                    initialIndentLength = initialIndentLength + indentIncrementLength,
                    indentIncrementLength = indentIncrementLength,
                )
            )
            builder.append("\n")
        }
    }
    builder.append("$initialIndent)")
    if (initialIndentLength != 0) {
        builder.append(",")
    }
    return builder.toString()
}

private fun Any.getPropertyValue(kParam: KParameter): Any? {
    return javaClass
        .kotlin
        .memberProperties
        .first { prop -> prop.name == kParam.name }.get(this)
}

fun <T : Any> KClass<T>.isPrimitive(): Boolean {
    return when (this) {
        Byte::class,
        Char::class,
        String::class,
        Boolean::class,
        Short::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class -> true
        else -> false
    }
}

fun Any.isPrimitive(): Boolean {
    return when (this) {
        is Byte,
        is Char,
        is String,
        is Boolean,
        is Short,
        is Int,
        is Long,
        is Float,
        is Double -> true
        else -> false
    }
}

fun <T> MutableList<T>.deepPrintMutableListReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    standalone: Boolean = true,
): String {
    return this.deepPrintListReflection(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = "mutableListOf",
        standalone = standalone,
    )
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

private fun <Any> Any?.deepPrintListItem(
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