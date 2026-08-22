package com.bradyaiello.deepprint

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties

/*
    List and MutableList look identical at runtime. They both are implemented by
    Java ArrayList, and the same goes for Set and MutableSet, and for Map and
    MutableMap. So we must default to the mutable constructor, which produces valid
    code for both the read-only and the mutable declaration.
    https://youtrack.jetbrains.com/issue/KT-23652/Reflection-Classifier-for-MutableListT-same-as-for-ListT
    https://youtrack.jetbrains.com/issue/KT-11754/Support-special-KClass-instances-for-mutable-collection-interfaces
 */
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
    val builder = StringBuilder()
    val constructor = kClass.constructors.first()
    val constructorCall = "${kClass.simpleName!!}(\n"
    builder.append("$initialIndent$constructorCall")
    val params = constructor.parameters

    params.forEach { kParam ->
        builder.append(
            deepPrintProperty(
                propName = kParam.name,
                propValue = this.getPropertyValue(kParam)!!,
                initialIndentLength = initialIndentLength,
                indentIncrementLength = indentIncrementLength,
            )
        )
    }
    builder.append("$initialIndent)")
    if (initialIndentLength != 0) {
        builder.append(",")
    }
    return builder.toString()
}

@Suppress("ReturnCount")
private fun deepPrintProperty(
    propName: String?,
    propValue: Any,
    initialIndentLength: Int,
    indentIncrementLength: Int,
): String {
    val assignment = "${initialIndentLength.indent()}${indentIncrementLength.indent()}$propName = "
    val startingIndent = initialIndentLength + indentIncrementLength
    fun config(constructor: String) = DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentIncrementLength,
        constructor = constructor,
    )

    if (propValue::class.isPrimitive()) {
        return "$assignment${deepPrintPrimitive(propValue)},\n"
    }
    when (propValue) {
        is List<*> -> return "$assignment${propValue.deepPrintListReflection(config("mutableListOf"))},\n"
        is Set<*> -> return "$assignment${propValue.deepPrintSetReflection(config("mutableSetOf"))},\n"
        is Map<*, *> -> return "$assignment${propValue.deepPrintMapReflection(config("mutableMapOf"))},\n"
        is Array<*> -> return "$assignment${propValue.deepPrintArrayReflection(config("arrayOf"))},\n"
    }
    val primitiveArray = propValue.deepPrintPrimitiveArrayReflectionOrNull(
        startingIndent = startingIndent,
        indentSize = indentIncrementLength,
    )
    if (primitiveArray != null) {
        return "$assignment$primitiveArray,\n"
    }
    return propValue.deepPrintReflection(
        initialIndentLength = startingIndent,
        indentIncrementLength = indentIncrementLength,
    ) + "\n"
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
