package com.bradyaiello.deepprint

import com.bradyaiello.deepprint.testclasses.deepPrint
import com.bradyaiello.deepprint.testobjects.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class BasicTest {

    @Test
    fun primitives()    {
        val expected = """
            AllTypes(
                aString = "Hello",
                aChar = 'A',
                anInt = 0,
                aByte = -1,
                aShort = 2,
                aLong = 1000,
                aBoolean = true,
                aFloat = 1234.0f,
                aDouble = 56789.0,
            )
        """.trimIndent()
        val actual = allTypes.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun simpleClass() {
        val expected = """
            SampleClass(
                x = 0.5f,
                y = 2.6f,
                name = "A point",
            )
        """.trimIndent()

        val actual = sample.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun annotatedClassWithAnnotatedProperty() {
        val expected = """
            SamplePersonClass(
                name = "Dave",
                sampleClass = 
                    SampleClass(
                        x = 0.5f,
                        y = 2.6f,
                        name = "A point",
                    ),
            )
        """.trimIndent()

        val actual = person.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun threeAnnotatedClassesDeepTwoAnnotatedClassesWide() {
        val expected = """
            ThreeClassesDeep3(
                age = 55,
                person = 
                    SamplePersonClass(
                        name = "Dave",
                        sampleClass = 
                            SampleClass(
                                x = 0.5f,
                                y = 2.6f,
                                name = "A point",
                            ),
                    ),
                sampleClass = 
                    SampleClass(
                        x = 0.5f,
                        y = 2.6f,
                        name = "A point",
                    ),
            )
        """.trimIndent()

        val actual = threeDeep2Wide.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableList() {
        val expected = """
            WithDeepPrintableList(
                name = "a name",
                surfers = listOf<Surfer>(
                    Surfer(
                        name = "Brady Aiello",
                        surfboard = 
                            Surfboard(
                                length = 11.5f,
                                width = 2.0f,
                                style = "longboard",
                            ),
                    ),
                    Surfer(
                        name = "Kelly Slater",
                        surfboard = 
                            Surfboard(
                                length = 5.9f,
                                width = 1.8f,
                                style = "shortboard",
                            ),
                    ),
                ),
            )
        """.trimIndent()

        val actual = withDeepPrintableList.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun fallbackToToStringIfNotAnnotated() {

        // ThreeDimLine is annotated.
        // ThreeDimCoordinate is not annotated.
        // In this case, fall back to the usual toString() method.
        val expected = """
            ThreeDimLine(
                start = ThreeDimCoordinate(x=0.7, y=32.5, z=92.1, label=Point A),
                end = ThreeDimCoordinate(x=99.2, y=154.7, z=23.3, label=Point B),
                lineLabel = "A -> B",
            )
        """.trimIndent()

        val actual = threeDimLine.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun annotatedProperty() {
        val actual = withAnnotatedProperty.deepPrint()
        println(actual)
    }
}