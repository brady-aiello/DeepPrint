package com.bradyaiello.deepprint.tostring

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToStringTest {

    @Test
    fun toStringIsReplacedByDeepPrint() {
        val expected = """
            Point(
                x = 1,
                y = 2,
            )
        """.trimIndent()

        assertEquals(expected, Point(1, 2).toString())
    }

    @Test
    fun stringInterpolationUsesIt() {
        // Interpolation calls toString(), so this proves the real member was replaced
        // rather than something merely resembling it being called explicitly.
        assertEquals(Point(1, 2).toString(), "${Point(1, 2)}")
    }

    @Test
    fun nestedDataClassesPrintDeeply() {
        val printed = Line(Point(3, 4), "edge").toString()

        assertTrue(printed.startsWith("Line("), printed)
        assertTrue(printed.contains("x = 3,"), printed)
        assertTrue(printed.contains("""label = "edge","""), printed)
    }

    @Test
    fun collectionsOfDataClassesUseItToo() {
        // The element's toString() is what a list prints with, so this is where an
        // overridden toString() becomes very visible.
        val printed = listOf(Point(1, 2)).toString()

        assertTrue(printed.contains("x = 1,"), printed)
    }

    @Test
    fun handWrittenToStringIsLeftAlone() {
        // Only the compiler-synthesised toString() is replaced. Overwriting one the
        // author wrote would be a bug, not a feature.
        assertEquals("hand written: 7", HandWritten(7).toString())
    }

    @Test
    fun noDeepPrintOptsOutOfTheOverride() {
        // NoDeepPrint has SOURCE retention, so this is also checking that IR can still
        // see it during the same compilation.
        assertEquals("OptedOut(value=9)", OptedOut(9).toString())
    }

    @Test
    fun toStringOfALocalClassPrintsItsExternalPropertyDeeply() {
        val holder = HoldsExternal(
            external = com.module.external.ExternalDataClass(
                name = "Bruce Wayne",
                age = 42,
                interests = listOf("tinkering"),
            ),
            id = "id-1",
        )

        assertTrue(holder.toString().contains("""name = "Bruce Wayne","""), holder.toString())
    }

    @Test
    fun toStringOfAnExternalClassItselfIsNotOverridden() {
        // The plugin rewrites toString() while compiling this module. A dependency's
        // class is already compiled, so its own toString() is out of reach. Printing it
        // through a local class still deep prints, via the generated deepPrint().
        val external = com.module.external.ExternalDataClass(
            name = "Bruce Wayne",
            age = 42,
            interests = listOf("tinkering"),
        )

        assertEquals(
            "ExternalDataClass(name=Bruce Wayne, age=42, interests=[tinkering])",
            external.toString(),
        )
    }

    /**
     * The lookup used kotlinFqName.parent(), which on a nested class is the outer class
     * rather than the package, so no deepPrint() was ever found and the class kept the
     * stock toString().
     */
    @Test
    fun toStringOfANestedDataClassIsRewritten() {
        val expected = """
            Outer.Nested(
                n = 1,
                s = "a",
            )
        """.trimIndent()

        assertEquals(expected, Outer.Nested(1, "a").toString())
    }

    /**
     * The receiver of a generic class's extension is Box<T> where T belongs to the
     * function, while the class's own defaultType has its own T. Comparing whole types
     * never matched, so this kept the stock toString() as well.
     */
    @Test
    fun toStringOfAGenericDataClassIsRewritten() {
        val expected = """
            Boxed(
                boxed = 7,
                label = "seven",
            )
        """.trimIndent()

        assertEquals(expected, Boxed(7, "seven").toString())
    }
}
