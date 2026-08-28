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
}
