package com.bradyaiello.deepprint.tostringmpp

import kotlin.test.Test
import kotlin.test.assertEquals

/** The same assertion on every target: does the IR rewrite reach non-JVM backends. */
class ToStringMppTest {
    @Test
    fun toStringIsReplacedOnEveryTarget() {
        val expected = """
            Point(
                x = 1,
                y = 2,
            )
        """.trimIndent()

        assertEquals(expected, Point(1, 2).toString())
    }
}
