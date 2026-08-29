package com.bradyaiello.deepprint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Null values, and values that are neither primitives, collections, nor `data class`es.
 */
class ReflectFallbackTest {

    @Test
    fun `null properties print as null`() {
        val container = NullableContainer(
            name = null,
            count = null,
            address = null,
            numbers = null,
        )
        val expected = """
            NullableContainer(
                name = null,
                count = null,
                address = null,
                numbers = null,
            )
        """.trimIndent()
        assertEquals(expected, container.deepPrintReflection())
    }

    @Test
    fun `nullable properties print normally when populated`() {
        val container = NullableContainer(
            name = "Brady",
            count = 3,
            address = Address(
                streetAddress = "414 Koshland Way",
                city = "Santa Cruz",
                state = "CA",
                zipCode = "95064"
            ),
            numbers = listOf(1, 2),
        )
        val expected = """
            NullableContainer(
                name = "Brady",
                count = 3,
                address = Address(
                    streetAddress = "414 Koshland Way",
                    city = "Santa Cruz",
                    state = "CA",
                    zipCode = "95064",
                ),
                numbers =  mutableListOf(
                    1,
                    2,
                ),
            )
        """.trimIndent()
        assertEquals(expected, container.deepPrintReflection())
    }

    @Test
    fun `enums print qualified, as a property, a list item and a map key`() {
        val container = EnumContainer(
            day = DayOfWeek.MONDAY,
            days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
            byDay = mapOf(DayOfWeek.MONDAY to "washing"),
        )
        val expected = """
            EnumContainer(
                day = DayOfWeek.MONDAY,
                days =  mutableListOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                ),
                byDay =  mutableMapOf(
                    DayOfWeek.MONDAY to "washing",
                ),
            )
        """.trimIndent()
        assertEquals(expected, container.deepPrintReflection())
    }

    @Test
    fun `an unsupported type falls back to toString instead of disappearing`() {
        val container = OpaqueContainer(id = Opaque("abc"), name = "x")
        // Not valid Kotlin, but the property is present and says what it holds. Before,
        // it was dropped from the output entirely.
        val expected = """
            OpaqueContainer(
                id = Opaque(abc),
                name = "x",
            )
        """.trimIndent()
        assertEquals(expected, container.deepPrintReflection())
    }

    @Test
    fun `a data class from another module reflects like any other`() {
        // Reflection reads the runtime class, so a module boundary is invisible to it.
        // This never needed the support KSP had to be given.
        val holder = HoldsExternalReflect(
            external = com.module.external.ExternalDataClass(
                name = "Bruce Wayne",
                age = 42,
                interests = listOf("tinkering"),
            ),
            id = "id-1",
        )
        val expected = """
            HoldsExternalReflect(
                external = ExternalDataClass(
                    name = "Bruce Wayne",
                    age = 42,
                    interests =  mutableListOf(
                        "tinkering",
                    ),
                ),
                id = "id-1",
            )
        """.trimIndent()

        assertEquals(expected, holder.deepPrintReflection())
    }

    /**
     * A `data object` reports isData like any other data class but has no constructor,
     * so this threw NoSuchElementException from constructors.first() rather than
     * printing anything.
     */
    @Test
    fun `an object prints as its name`() {
        val expected = """
            HoldsObjects(
                dataObject = LoneDataObject,
                plain = PlainObject,
                nested = Marker.Present,
                id = "x",
            )
        """.trimIndent()

        val actual = HoldsObjects(
            dataObject = LoneDataObject,
            plain = PlainObject,
            nested = Marker.Present,
            id = "x",
        ).deepPrintReflection()

        assertEquals(expected, actual)
    }

    @Test
    fun `a data object prints as its name when printed directly`() {
        assertEquals("LoneDataObject", LoneDataObject.deepPrintReflection())
    }

    /** A nested object needs its outer name, or the output does not resolve. */
    @Test
    fun `a nested object is qualified through its nesting`() {
        assertEquals("Marker.Present", Marker.Present.deepPrintReflection())
    }

    /**
     * A value class fell through to toString() and printed `UserId(raw=abc)`, which
     * reads correctly in a log and is not valid Kotlin -- the string lost its quotes.
     * Char had the same problem, and Double had it in reverse: `1.5` is fine but only
     * by accident.
     */
    @Test
    fun `a value class prints as a call to its own constructor`() {
        val expected = """
            HoldsValueClasses(
                id = UserId(raw = "abc"),
                distance = Meters(amount = 1.5),
                initial = Initial(letter = 'K'),
                label = "x",
            )
        """.trimIndent()

        val actual = HoldsValueClasses(
            id = UserId("abc"),
            distance = Meters(1.5),
            initial = Initial('K'),
            label = "x",
        ).deepPrintReflection()

        assertEquals(expected, actual)
    }

    /**
     * Reflection sees the runtime subclass, so it can deep print through a sealed
     * parent type -- but the name has to carry its nesting or the output does not
     * resolve. `Absent(` alone is not a thing; `Marker.Absent(` is.
     */
    @Test
    fun `a sealed subclass is qualified through its parent`() {
        val expected = """
            HoldsSealed(
                shape = Marker.Absent(
                    reason = "gone",
                ),
                level = EnumHost.Level.HIGH,
                id = 7,
            )
        """.trimIndent()

        val actual = HoldsSealed(
            shape = Marker.Absent("gone"),
            level = EnumHost.Level.HIGH,
            id = 7,
        ).deepPrintReflection()

        assertEquals(expected, actual)
    }

    /** A cycle used to recurse until the stack ran out. */
    @Test
    fun `a cycle prints a TODO instead of overflowing the stack`() {
        val node = CycleNode("a", null)
        node.next = node

        val expected = """
            CycleNode(
                name = "a",
                next = TODO("DeepPrint: cycle back to CycleNode"),
            )
        """.trimIndent()

        assertEquals(expected, node.deepPrintReflection())
    }

    /** Collections recurse through the same entry point, so they are covered too. */
    @Test
    fun `a cycle through a collection is caught as well`() {
        val parent = CycleParent("p", mutableListOf())
        parent.children.add(parent)

        val actual = CycleParent("p", mutableListOf(parent)).deepPrintReflection()

        assertTrue(
            actual.contains("""TODO("DeepPrint: cycle back to CycleParent")"""),
            "expected a cycle marker, got:\n$actual",
        )
    }

    /**
     * The same object twice is repetition, not a cycle. Guarding by equality rather than
     * by identity, or forgetting to drop the entry on the way out, would call the second
     * one a cycle and print a TODO() where real data belongs.
     */
    @Test
    fun `the same object appearing twice still prints in full`() {
        val shared = CycleNode("shared", null)

        val expected = """
            Repeated(
                first = CycleNode(
                    name = "shared",
                    next = null,
                ),
                second = CycleNode(
                    name = "shared",
                    next = null,
                ),
            )
        """.trimIndent()

        assertEquals(expected, Repeated(shared, shared).deepPrintReflection())
    }
}
