package com.bradyaiello.deepprint.noannotations

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Nothing in this module is annotated with @DeepPrint. The processor runs with
 * processAllDataClasses, so every eligible data class gets a deepPrint().
 */
class NoAnnotationTest {

    @Test
    fun unannotatedClassesDeepPrintIncludingTheirProperties() {
        // Neither Line nor Point is annotated. Under the annotated mode, start and end
        // would have fallen back to toString().
        val expected = """
            Line(
                start = 
                    Point(
                        x = 1,
                        y = 2,
                    ),
                end = 
                    Point(
                        x = 3,
                        y = 4,
                    ),
                label = "a line",
            )
        """.trimIndent()

        assertEquals(expected, Line(Point(1, 2), Point(3, 4), "a line").deepPrint())
    }

    @Test
    fun nestedClassIsQualified() {
        // A bare `Inner` would not resolve at package level, in the receiver or in the
        // printed constructor call.
        val expected = """
            Outer.Inner(
                name = "x",
            )
        """.trimIndent()

        assertEquals(expected, Outer.Inner("x").deepPrint())
    }

    @Test
    fun noDeepPrintExcludesAClass() {
        // Excluded gets no deepPrint(), so the property falls back to toString().
        val expected = """
            HoldsExcluded(
                excluded = Excluded(payload=p),
                id = 7,
            )
        """.trimIndent()

        assertEquals(expected, HoldsExcluded(Excluded("p"), 7).deepPrint())
    }

    @Test
    fun internalClassesAreIncluded() {
        // internal is visible across the module, unlike private.
        val expected = """
            InternalPoint(
                x = 5,
            )
        """.trimIndent()

        assertEquals(expected, InternalPoint(5).deepPrint())
    }
}
