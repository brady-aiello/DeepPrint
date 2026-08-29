package com.bradyaiello.deepprint

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/*
    How a single value renders once the traversal in Reflection.kt has decided it is not
    a data class, a collection, or an array. Everything here returns one line.
 */

/**
 * A `value class` rendered as a call to its own constructor, eg. `UserId(raw = "abc")`,
 * or null if [this] is not one.
 *
 * Without this a value class fell through to toString() and printed `UserId(raw=abc)`,
 * which reads correctly in a log and is not valid Kotlin: the wrapped string has lost
 * its quotes. The wrapped value is rendered by the same rules as any other single
 * value, so a value class around a Char or a Long comes out right too.
 */
internal fun Any.deepPrintValueClassOrNull(): String? {
    val kClass = this::class
    val parameterName = kClass.takeIf { it.isValue }
        ?.primaryConstructor
        ?.parameters
        ?.singleOrNull()
        ?.name
    val wrapped = parameterName?.let { name ->
        kClass.memberProperties.firstOrNull { it.name == name }?.getter?.call(this)
    }
    return parameterName?.let { "${kClass.printableName()}($it = ${wrapped.deepPrintSingleValue()})" }
}

/** One value on one line: a primitive, an object, a nested value class, or toString(). */
private fun Any?.deepPrintSingleValue(): String = when {
    this == null -> "null"
    this::class.isPrimitive() -> deepPrintPrimitive(this)
    else -> deepPrintObjectOrNull() ?: deepPrintValueClassOrNull() ?: deepPrintUnsupportedReflection()
}

/**
 * The name of [this] if it is an `object`, otherwise null.
 *
 * Covers a plain `object` as well as a `data object`; the alternative for a plain one
 * was `Singleton@1b2c3d`, which is never what anyone wants to paste back into source.
 */
internal fun Any.deepPrintObjectOrNull(): String? =
    if (this::class.objectInstance == null) null else this::class.printableName()

/**
 * The class name, qualified through any nesting: `Marker.Present`, not `Present`.
 *
 * A nested name printed bare would not resolve -- the same reason the enum case below
 * prints `DayOfWeek.MONDAY` rather than `MONDAY`.
 */
internal fun KClass<*>.printableName(): String =
    java.name.substringAfterLast('.').replace('$', '.')

/**
 * Last resort for a value that is neither a primitive, a supported collection, an
 * object, a value class, nor a `data class`: an enum, a java.time value, a UUID. There
 * is nothing to recurse into, but dropping the property would produce output that looks
 * complete and is not, so print what the value knows how to say about itself.
 *
 * Only enums come back out as valid Kotlin. Everything else is a readable placeholder
 * that has to be filled in by hand.
 */
internal fun Any.deepPrintUnsupportedReflection(): String = when (this) {
    is Enum<*> -> {
        // An enum constant with a body is an anonymous subclass of the enum itself.
        val enumClass = if (javaClass.isEnum) javaClass else javaClass.superclass
        "${enumClass.kotlin.printableName()}.$name"
    }
    else -> toString()
}
