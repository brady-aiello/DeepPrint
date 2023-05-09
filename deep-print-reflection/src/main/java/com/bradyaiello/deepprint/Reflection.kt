package com.bradyaiello.deepprint

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

fun Any?.deepPrintReflection(
    initialIndentLength: Int = 0,
    indentIncrementLength: Int = 4,
) : String {
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
        }  else if (propValue is List<*>) {
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
                    propValue.deepPrintListReflect(
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

fun <T : Any>KClass<T>.isPrimitive(): Boolean {
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

fun <T> MutableList<T>.deepPrintMutableListReflect(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    standalone: Boolean = true,
): String {
    return this.deepPrintListReflect(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = "mutableListOf",
        standalone = standalone,
    )
}

fun <T> List<T>.deepPrintListReflect(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "listOf",
    standalone: Boolean = true,
): String {
    val stringBuilder = StringBuilder()
    val start = startingIndent.indent()
    val indent = indentSize.indent()
    val prefix = if (standalone) start else " "
    stringBuilder.append("${prefix}$constructor(\n")
    val totalIndent = start + indent
    this.forEach { value ->
        if (value == null) {
            stringBuilder.append("${totalIndent}null,\n")
        } else if (value!!::class.isPrimitive()) {
            stringBuilder.append("${totalIndent}${deepPrintPrimitive(value)},\n")
        } else {
            stringBuilder.append(
                value.deepPrintReflection(
                    initialIndentLength = startingIndent + indentSize,
                    indentIncrementLength = indentSize,
                ) + "\n"
            )
        }
    }
    stringBuilder.append("${start})")
    return stringBuilder.toString()
}
