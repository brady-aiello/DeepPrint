@file:Suppress("TooManyFunctions")

package com.bradyaiello.deepprint

/*
 * ByteArray, IntArray and the rest have no common supertype and are not Array<T>,
 * so each one needs its own entry points. They all hold primitives, so the contents
 * never need to recurse the way a List or an Array of data classes does.
 *
 * Eight types times two entry points each is what puts this file over detekt's
 * TooManyFunctions threshold; there is no shorter way to cover them.
 */

fun ByteArray.deepPrintByteArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "byteArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun ByteArray.deepPrintByteArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "byteArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun ShortArray.deepPrintShortArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "shortArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun ShortArray.deepPrintShortArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "shortArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun IntArray.deepPrintIntArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "intArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun IntArray.deepPrintIntArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "intArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun LongArray.deepPrintLongArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "longArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun LongArray.deepPrintLongArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "longArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun FloatArray.deepPrintFloatArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "floatArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun FloatArray.deepPrintFloatArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "floatArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun DoubleArray.deepPrintDoubleArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "doubleArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun DoubleArray.deepPrintDoubleArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "doubleArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun BooleanArray.deepPrintBooleanArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "booleanArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun BooleanArray.deepPrintBooleanArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "booleanArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

fun CharArray.deepPrintCharArrayReflection(
    deepPrintReflectConfig: DeepPrintReflectConfig = DeepPrintReflectConfig(
        constructor = "charArrayOf",
        standalone = true,
    )
): String = asIterable().deepPrintPrimitiveItems(deepPrintReflectConfig)

fun CharArray.deepPrintCharArrayReflection(
    startingIndent: Int = 0,
    indentSize: Int = 4,
    constructor: String = "charArrayOf",
    standalone: Boolean = true,
): String = asIterable().deepPrintPrimitiveItems(
    DeepPrintReflectConfig(
        startingIndent = startingIndent,
        indentSize = indentSize,
        constructor = constructor,
        standalone = standalone,
    )
)

@OptIn(ExperimentalUnsignedTypes::class)
internal fun Any.isPrimitiveArray(): Boolean = when (this) {
    is ByteArray, is ShortArray, is IntArray, is LongArray,
    is FloatArray, is DoubleArray, is BooleanArray, is CharArray,
    is UByteArray, is UShortArray, is UIntArray, is ULongArray -> true
    else -> false
}

/**
 * Prints [this] as a primitive array literal, or returns null if it is not one of the
 * eight primitive array types. Used to render a `data class` property, where the only
 * thing known about the value is that it is [Any].
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal fun Any.deepPrintPrimitiveArrayReflectionOrNull(
    startingIndent: Int,
    indentSize: Int,
    standalone: Boolean = false,
): String? {
    val (items, constructor) = when (this) {
        is ByteArray -> asIterable() to "byteArrayOf"
        is ShortArray -> asIterable() to "shortArrayOf"
        is IntArray -> asIterable() to "intArrayOf"
        is LongArray -> asIterable() to "longArrayOf"
        is FloatArray -> asIterable() to "floatArrayOf"
        is DoubleArray -> asIterable() to "doubleArrayOf"
        is BooleanArray -> asIterable() to "booleanArrayOf"
        is CharArray -> asIterable() to "charArrayOf"
        is UByteArray -> asIterable() to "ubyteArrayOf"
        is UShortArray -> asIterable() to "ushortArrayOf"
        is UIntArray -> asIterable() to "uintArrayOf"
        is ULongArray -> asIterable() to "ulongArrayOf"
        else -> return null
    }
    return items.deepPrintPrimitiveItems(
        DeepPrintReflectConfig(
            startingIndent = startingIndent,
            indentSize = indentSize,
            constructor = constructor,
            standalone = standalone,
        )
    )
}

private fun Iterable<Any?>.deepPrintPrimitiveItems(
    deepPrintReflectConfig: DeepPrintReflectConfig,
): String = with(deepPrintReflectConfig) {
    val stringBuilder = StringBuilder()
    val start = startingIndent.indent()
    val prefix = if (standalone) start else " "
    stringBuilder.append("$prefix$constructor(\n")
    val itemIndent = start + indentSize.indent()
    forEach { value ->
        stringBuilder.append("$itemIndent${deepPrintPrimitive(value)},\n")
    }
    stringBuilder.append("$start)")
    stringBuilder.toString()
}
