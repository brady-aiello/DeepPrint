package com.bradyaiello.deepprint

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

fun Any?.deepPrintReflection(
    initialIndentLength: Int = 0,
    indentIncrementLength: Int = 4,
) : String {
    if (this == null) {
        return ""
    }
    val kClass = this::class
    if (!kClass.isData) {
        return ""
    }
    
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

