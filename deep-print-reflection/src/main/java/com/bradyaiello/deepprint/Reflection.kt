package com.bradyaiello.deepprint

import java.util.IdentityHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

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
        withCycleGuard(initialIndentLength) {
            deepPrintDataClassReflection(initialIndentLength, indentIncrementLength)
        }
    }
}

/**
 * Runs [print] unless [this] is already being printed further up the same call, in which
 * case the graph has a cycle and printing it would recurse until the stack ran out. That
 * is what used to happen: a `data class` holding a reference back to itself met
 * deepPrintReflection() with a StackOverflowError.
 *
 * A cycle cannot be written as a constructor call at all -- the object has to exist
 * before it can be referenced -- so there is no output that would reconstruct it. What
 * comes out instead is a TODO() naming the class:
 *
 *     next = TODO("DeepPrint: cycle back to Node"),
 *
 * which compiles wherever the property sits, nullable or not, since TODO() is Nothing.
 * It is deliberately something you cannot run and cannot miss, in keeping with the way
 * an unsupported type prints a placeholder rather than throwing.
 *
 * Identity rather than equality: two equal data classes are ordinary repetition, and a
 * HashSet would call the second one a cycle. The entry is removed on the way out, so the
 * same object appearing twice side by side still prints in full both times. Only an
 * ancestor counts.
 */
private fun <T : Any> T.withCycleGuard(initialIndentLength: Int, print: () -> String): String {
    val inProgress = ancestors.get()
    if (inProgress.containsKey(this)) {
        val marker = "TODO(\"DeepPrint: cycle back to ${this::class.printableName()}\")"
        return if (initialIndentLength == 0) marker
        else "${initialIndentLength.indent()}$marker,"
    }
    inProgress[this] = Unit
    return try {
        print()
    } finally {
        inProgress.remove(this)
        // Leaving an empty map on the thread would outlive the call that needed it.
        if (inProgress.isEmpty()) ancestors.remove()
    }
}

/** The data classes between the top of the current print and the value being printed. */
private val ancestors = ThreadLocal.withInitial { IdentityHashMap<Any, Unit>() }

private fun Any.deepPrintDataClassReflection(
    initialIndentLength: Int,
    indentIncrementLength: Int,
): String {
    val kClass = this::class
    val initialIndent = if (initialIndentLength == 0) ""
    else initialIndentLength.indent()
    val builder = StringBuilder()
    val constructor = kClass.constructors.first()
    // Qualified through any nesting. A sealed subclass reached through its parent type
    // is the common case: printing `Circle(` rather than `Shape.Circle(` produces output
    // that does not resolve, and reflection is the one implementation that can see which
    // subclass it actually has.
    val constructorCall = "${kClass.printableName()}(\n"
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
    propValue.deepPrintValueClassOrNull()?.let { rendered ->
        return "$assignment$rendered,\n"
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
    this::class.objectInstance != null || this::class.isValue -> true
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
