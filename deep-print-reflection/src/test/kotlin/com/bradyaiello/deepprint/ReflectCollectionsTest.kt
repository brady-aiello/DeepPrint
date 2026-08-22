package com.bradyaiello.deepprint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Sets and the primitive arrays. The older collection types are covered in
 * [ReflectionTest].
 */
class ReflectCollectionsTest {

    @Test
    fun `deep print set of Integers`() {
        val mySet = setOf(1, 2, 3, 4, 5)
        val expected = """
            setOf(
                1,
                2,
                3,
                4,
                5,
            )
        """.trimIndent()
        val actual = mySet.deepPrintSetReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print mutable set of Integers`() {
        val mySet = mutableSetOf(1, 2, 3, 4, 5)
        val expected = """
            mutableSetOf(
                1,
                2,
                3,
                4,
                5,
            )
        """.trimIndent()
        val actual = mySet.deepPrintMutableSetReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print standalone Set of data classes`() {
        val mySet = createPeople().toSet()
        val expected = """
            setOf(
                Person(
                    name = "Brady",
                    age = 38,
                    address = Address(
                        streetAddress = "414 Koshland Way",
                        city = "Santa Cruz",
                        state = "CA",
                        zipCode = "95064",
                    ),
                ),
                Person(
                    name = "Joe",
                    age = 80,
                    address = Address(
                        streetAddress = "1600 Pennsylvania Avenue, N.W.",
                        city = "Washington",
                        state = "DC",
                        zipCode = "20500",
                    ),
                ),
            )
        """.trimIndent()
        val actual = mySet.deepPrintSetReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with a set of Integers`() {
        val setContainer = SetContainer(
            someString = "Some String",
            numbers = setOf(1, 2, 3, 4, 5)
        )
        // Set and MutableSet are the same class at runtime, so mutableSetOf() is
        // printed: it is the constructor that is valid for both.
        val expected = """
            SetContainer(
                someString = "Some String",
                numbers =  mutableSetOf(
                    1,
                    2,
                    3,
                    4,
                    5,
                ),
            )
        """.trimIndent()
        val actual = setContainer.deepPrintReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with a set of data classes`() {
        val withSetOfDataClasses = WithSetOfDataClasses(
            id = "12345",
            people = createPeople().toSet()
        )
        val expected = """
            WithSetOfDataClasses(
                id = "12345",
                people =  mutableSetOf(
                    Person(
                        name = "Brady",
                        age = 38,
                        address = Address(
                            streetAddress = "414 Koshland Way",
                            city = "Santa Cruz",
                            state = "CA",
                            zipCode = "95064",
                        ),
                    ),
                    Person(
                        name = "Joe",
                        age = 80,
                        address = Address(
                            streetAddress = "1600 Pennsylvania Avenue, N.W.",
                            city = "Washington",
                            state = "DC",
                            zipCode = "20500",
                        ),
                    ),
                ),
            )
        """.trimIndent()
        val actual = withSetOfDataClasses.deepPrintReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print standalone IntArray`() {
        val myArray = intArrayOf(1, 2, 3, 4, 5)
        val expected = """
            intArrayOf(
                1,
                2,
                3,
                4,
                5,
            )
        """.trimIndent()
        val actual = myArray.deepPrintIntArrayReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print standalone FloatArray`() {
        val myArray = floatArrayOf(1f, 2.5f)
        val expected = """
            floatArrayOf(
                1.0f,
                2.5f,
            )
        """.trimIndent()
        val actual = myArray.deepPrintFloatArrayReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print standalone CharArray`() {
        val myArray = charArrayOf('A', 'B')
        val expected = """
            charArrayOf(
                'A',
                'B',
            )
        """.trimIndent()
        val actual = myArray.deepPrintCharArrayReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with primitive arrays`() {
        val primitiveArraysContainer = PrimitiveArraysContainer(
            bytes = byteArrayOf(-1, 0, 1),
            shorts = shortArrayOf(2, 3),
            ints = intArrayOf(1, 2, 3),
            longs = longArrayOf(1000L, 2000L),
            floats = floatArrayOf(26.2f, 1f),
            doubles = doubleArrayOf(26.2, 1.0),
            booleans = booleanArrayOf(true, false),
            chars = charArrayOf('a', 'b'),
        )
        val expected = """
            PrimitiveArraysContainer(
                bytes =  byteArrayOf(
                    -1,
                    0,
                    1,
                ),
                shorts =  shortArrayOf(
                    2,
                    3,
                ),
                ints =  intArrayOf(
                    1,
                    2,
                    3,
                ),
                longs =  longArrayOf(
                    1000,
                    2000,
                ),
                floats =  floatArrayOf(
                    26.2f,
                    1.0f,
                ),
                doubles =  doubleArrayOf(
                    26.2,
                    1.0,
                ),
                booleans =  booleanArrayOf(
                    true,
                    false,
                ),
                chars =  charArrayOf(
                    'a',
                    'b',
                ),
            )
        """.trimIndent()
        val actual = primitiveArraysContainer.deepPrintReflection()
        assertEquals(expected, actual)
    }

    @Test
    fun `deep print data class with unsigned types`() {
        val container = UnsignedContainer(
            int = 3u,
            long = 4u,
            ints = uintArrayOf(5u, 6u),
        )
        val expected = """
            UnsignedContainer(
                int = 3u,
                long = 4u,
                ints =  uintArrayOf(
                    5u,
                    6u,
                ),
            )
        """.trimIndent()
        assertEquals(expected, container.deepPrintReflection())
    }

    @Test
    fun `deep print data class with nested collections`() {
        val container = NestedContainer(
            listOfLists = listOf(listOf(0, 1)),
            listOfArrays = listOf(intArrayOf(1, 2)),
            mapValueList = mapOf("a" to listOf(1, 2)),
            mapOfMaps = mapOf("a" to mapOf("b" to 1)),
            setOfLists = setOf(listOf(9)),
        )
        val expected = """
            NestedContainer(
                listOfLists =  mutableListOf(
                    mutableListOf(
                        0,
                        1,
                    ),
                ),
                listOfArrays =  mutableListOf(
                    intArrayOf(
                        1,
                        2,
                    ),
                ),
                mapValueList =  mutableMapOf(
                    "a" to
                        mutableListOf(
                            1,
                            2,
                        ),
                ),
                mapOfMaps =  mutableMapOf(
                    "a" to
                        mutableMapOf(
                            "b" to 1,
                        ),
                ),
                setOfLists =  mutableSetOf(
                    mutableListOf(
                        9,
                    ),
                ),
            )
        """.trimIndent()
        assertEquals(expected, container.deepPrintReflection())
    }
}
