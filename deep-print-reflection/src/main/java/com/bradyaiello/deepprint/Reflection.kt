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
    val startingIndentForParams = initialIndentLength + indentIncrementLength
    val listConfig = DeepPrintReflectConfig(startingIndent = startingIndentForParams, constructor = "mutableListOf")
    val mapConfig = DeepPrintReflectConfig(startingIndent = startingIndentForParams, constructor = "mutableMapOf")
    val arrayConfig = DeepPrintReflectConfig(startingIndent = startingIndentForParams, constructor = "arrayOf")
    
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
                    propValue.deepPrintListReflection(listConfig)
                },\n"
            )
        } else if (propValue is Map<*,*>) {
            builder.append(
                "$initialIndent$indentIncrement$propName = ${
                    propValue.deepPrintMapReflection(mapConfig)
                },\n"
            )
        } else if (propValue is Array<*>) {
            builder.append(
                "$initialIndent$indentIncrement$propName = ${
                    propValue.deepPrintArrayReflection(arrayConfig)
                },\n"
            )
        }
        
        else {
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

internal fun <T : Any> KClass<T>.isPrimitive(): Boolean {
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

data class DeepPrintReflectConfig(
    val startingIndent: Int = 0,
    val indentSize: Int = 4,
    val constructor: String,
    val standalone: Boolean = false,
)
