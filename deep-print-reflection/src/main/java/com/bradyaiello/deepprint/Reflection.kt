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
    // A `data object` reports isData like any other data class but has no constructor,
    // so the constructor path below threw NoSuchElementException on it. There is
    // nothing to reconstruct: an object prints as its own name.
    val objectName = deepPrintObjectOrNull()
    return if (objectName != null) {
        if (initialIndentLength == 0) objectName else "${initialIndentLength.indent()}$objectName,"
    } else {
        deepPrintDataClassReflection(initialIndentLength, indentIncrementLength)
    }
}

private fun Any.deepPrintDataClassReflection(
    initialIndentLength: Int,
    indentIncrementLength: Int,
): String {
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
                propValue = this.getPropertyValue(kParam),
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
    propValue: Any?,
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

    if (propValue == null) {
        return "${assignment}null,\n"
    }
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
    propValue.deepPrintObjectOrNull()?.let { name ->
        return "$assignment$name,\n"
    }
    if (!propValue::class.isData) {
        return "$assignment${propValue.deepPrintUnsupportedReflection()},\n"
    }
    // deepPrintReflection() indents the constructor call itself, but the name and the
    // `=` are already on the line, so that leading indent has to come back off.
    val nested = propValue.deepPrintReflection(
        initialIndentLength = startingIndent,
        indentIncrementLength = indentIncrementLength,
    )
    return assignment + nested.removePrefix(startingIndent.indent()) + "\n"
}

/**
 * Last resort for a value that is neither a primitive, a supported collection, nor a
 * `data class`: an enum, a java.time value, a UUID. There is nothing to recurse into,
 * but dropping the property would produce output that looks complete and is not, so
 * print what the value knows how to say about itself.
 *
 * Only enums come back out as valid Kotlin. Everything else is a readable placeholder
 * that has to be filled in by hand.
 */
/**
 * True when the value occupies a single line: primitives, enums, and anything else
 * that falls back to toString(). Collections and nested `data class`es open a block
 * and so cannot share a line with a map key.
 */
internal fun Any?.printsInline(): Boolean = when {
    this == null -> true
    isPrimitive() -> true
    this is Collection<*> || this is Map<*, *> || this is Array<*> || isPrimitiveArray() -> false
    this::class.objectInstance != null -> true
    else -> !this::class.isData
}

/**
 * Renders [this] as a collection literal indented to [startingIndent], or returns null
 * when it is not a collection. Used where a collection appears inside another
 * collection or a map: those positions know nothing about the element type, so the
 * recursion has to be driven from the runtime value.
 */
internal fun Any.deepPrintNestedCollectionOrNull(
    startingIndent: Int,
    indentSize: Int,
): String? = when (this) {
    is List<*> -> deepPrintListReflection(startingIndent, indentSize, "mutableListOf", standalone = true)
    is Set<*> -> deepPrintSetReflection(startingIndent, indentSize, "mutableSetOf", standalone = true)
    is Map<*, *> -> deepPrintMapReflection(startingIndent, indentSize, "mutableMapOf", standalone = true)
    is Array<*> -> deepPrintArrayReflection(startingIndent, indentSize, "arrayOf", standalone = true)
    else -> deepPrintPrimitiveArrayReflectionOrNull(startingIndent, indentSize, standalone = true)
}

/**
 * The name of [this] if it is an `object`, otherwise null.
 *
 * Qualified through its nesting, because a nested object printed as `Dot` would not
 * resolve while `Shape.Dot` does -- the same reason the enum case below prints
 * `DayOfWeek.MONDAY` rather than `MONDAY`. Covers a plain `object` as well as a
 * `data object`; the alternative for a plain one was `Singleton@1b2c3d`, which is
 * never what anyone wants to paste back into source.
 */
internal fun Any.deepPrintObjectOrNull(): String? =
    if (this::class.objectInstance == null) null
    else javaClass.name.substringAfterLast('.').replace('$', '.')

internal fun Any.deepPrintUnsupportedReflection(): String = when (this) {
    is Enum<*> -> {
        // An enum constant with a body is an anonymous subclass of the enum itself.
        val enumClass = if (javaClass.isEnum) javaClass else javaClass.superclass
        "${enumClass.simpleName}.$name"
    }
    else -> toString()
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
        Double::class,
        UByte::class,
        UShort::class,
        UInt::class,
        ULong::class -> true
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
        is Double,
        is UByte,
        is UShort,
        is UInt,
        is ULong -> true
        else -> false
    }
}

data class DeepPrintReflectConfig(
    val startingIndent: Int = 0,
    val indentSize: Int = 4,
    val constructor: String,
    val standalone: Boolean = false,
)
