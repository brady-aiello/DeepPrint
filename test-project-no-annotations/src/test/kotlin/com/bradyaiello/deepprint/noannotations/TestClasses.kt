package com.bradyaiello.deepprint.noannotations

import com.bradyaiello.deepprint.NoDeepPrint

data class Point(val x: Int, val y: Int)

/** Nothing here is annotated, and the nested Points still deep print. */
data class Line(val start: Point, val end: Point, val label: String)

class Outer {
    /** Referencing this as a bare `Inner` would not resolve at package level. */
    data class Inner(val name: String)
}

internal data class InternalPoint(val x: Int)

/**
 * File-private, so a generated extension in a separate file could not see it. This
 * module compiling at all is the assertion that it was skipped.
 */
private data class Secret(val token: String)

@NoDeepPrint
data class Excluded(val payload: String)

data class HoldsExcluded(val excluded: Excluded, val id: Int)

@Suppress("UnusedPrivateProperty")
private val keepSecretReferenced = Secret("unused")
