package com.bradyaiello.deepprint.tostring

import com.bradyaiello.deepprint.DeepPrint

// Annotated because this spike branches from main, which does not yet have the
// no-annotation mode. The IR rewrite being spiked is independent of how deepPrint()
// comes to exist.
@DeepPrint
data class Point(val x: Int, val y: Int)

@DeepPrint
data class Line(val start: Point, val label: String)

/** Declares its own toString(). The plugin must leave it alone. */
@DeepPrint
data class HandWritten(val value: Int) {
    override fun toString(): String = "hand written: $value"
}

/** Opted out. Its toString() must stay the compiler's own. */
@DeepPrint
@com.bradyaiello.deepprint.NoDeepPrint
data class OptedOut(val value: Int)

/** Holds a data class that lives in another module. */
@DeepPrint
data class HoldsExternal(
    val external: com.module.external.ExternalDataClass,
    val id: String,
)

class Outer {
    @DeepPrint
    data class Nested(val n: Int, val s: String)
}

@DeepPrint
data class Boxed<T>(val boxed: T, val label: String)
