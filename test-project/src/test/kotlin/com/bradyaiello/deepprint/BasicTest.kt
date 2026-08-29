package com.bradyaiello.deepprint

import com.bradyaiello.deepprint.testclasses.deepPrint
import com.bradyaiello.deepprint.testobjects.allTypes
import com.bradyaiello.deepprint.testobjects.person
import com.bradyaiello.deepprint.testobjects.primitivesMap
import com.bradyaiello.deepprint.testobjects.sample
import com.bradyaiello.deepprint.testobjects.threeDeep2Wide
import com.bradyaiello.deepprint.testobjects.threeDimLine
import com.bradyaiello.deepprint.testobjects.withAList
import com.bradyaiello.deepprint.testobjects.withAMap
import com.bradyaiello.deepprint.testobjects.withAMapDataClasses
import com.bradyaiello.deepprint.testobjects.withAMutableList
import com.bradyaiello.deepprint.testobjects.withAMutableMap
import com.bradyaiello.deepprint.testobjects.withAMutableMapDataClasses
import com.bradyaiello.deepprint.testobjects.withAMutableSet
import com.bradyaiello.deepprint.testobjects.withASet
import com.bradyaiello.deepprint.testobjects.withAnArray
import com.bradyaiello.deepprint.testobjects.withAnEmptyDeepPrintableSet
import com.bradyaiello.deepprint.testobjects.withAnnotatedProperty
import com.bradyaiello.deepprint.testobjects.withCollectionMapValues
import com.bradyaiello.deepprint.testobjects.withDeepPrintableArray
import com.bradyaiello.deepprint.testobjects.withDeepPrintableList
import com.bradyaiello.deepprint.testobjects.withDeepPrintableMutableList
import com.bradyaiello.deepprint.testobjects.withDeepPrintableMutableSet
import com.bradyaiello.deepprint.testobjects.withDeepPrintableSet
import com.bradyaiello.deepprint.testobjects.withEnums
import com.bradyaiello.deepprint.testobjects.withJdkCollections
import com.bradyaiello.deepprint.testobjects.withNullablesEmpty
import com.bradyaiello.deepprint.testobjects.withNullablesPopulated
import com.bradyaiello.deepprint.testobjects.withPrimitiveArrays
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun deepPrintableIntList() {
        val expected = """
            WithAList(
                name = "some list",
                items = listOf<Int>( 0, 1, 2, 3, 4,),
            )
        """.trimIndent()
        val actual = withAList.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableIntMutableList() {
        val expected = """
            WithAMutableList(
                name = "some list",
                items = mutableListOf<Int>( 0, 1, 2, 3, 4,),
            )
        """.trimIndent()
        val actual = withAMutableList.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableIntArray() {
        val expected = """
            WithAnArray(
                name = "some list",
                items = arrayOf<Int>( 0, 1, 2, 3, 4,),
            )
        """.trimIndent()
        val actual = withAnArray.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableList() {
        val expected = """
            WithDeepPrintableList(
                name = "a name",
                surfers = listOf<Surfer>(
                    Surfer(
                        name = "Honolua Blomfield",
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
    fun deepPrintableMutableList() {
        val expected = """
            WithDeepPrintableMutableList(
                name = "a name",
                surfers = mutableListOf<Surfer>(
                    Surfer(
                        name = "Honolua Blomfield",
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

        val actual = withDeepPrintableMutableList.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableArray() {
        val expected = """
            WithDeepPrintableArray(
                name = "a name",
                surfers = arrayOf<Surfer>(
                    Surfer(
                        name = "Honolua Blomfield",
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

        val actual = withDeepPrintableArray.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableIntSet() {
        val expected = """
            WithASet(
                name = "some set",
                items = setOf<Int>( 0, 1, 2, 3, 4,),
            )
        """.trimIndent()

        val actual = withASet.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableIntMutableSet() {
        val expected = """
            WithAMutableSet(
                name = "some set",
                items = mutableSetOf<Int>( 0, 1, 2, 3, 4,),
            )
        """.trimIndent()

        val actual = withAMutableSet.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableSet() {
        val expected = """
            WithDeepPrintableSet(
                name = "a name",
                surfers = setOf<Surfer>(
                    Surfer(
                        name = "Honolua Blomfield",
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

        val actual = withDeepPrintableSet.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun deepPrintableMutableSet() {
        val expected = """
            WithDeepPrintableMutableSet(
                name = "a name",
                surfers = mutableSetOf<Surfer>(
                    Surfer(
                        name = "Honolua Blomfield",
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

        val actual = withDeepPrintableMutableSet.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun emptyDeepPrintableSet() {
        val expected = """
            WithDeepPrintableSet(
                name = "a name",
                surfers = setOf<Surfer>(
                ),
            )
        """.trimIndent()

        val actual = withAnEmptyDeepPrintableSet.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun primitiveArrays() {
        val expected = """
            WithPrimitiveArrays(
                bytes = byteArrayOf( -1, 0, 1,),
                shorts = shortArrayOf( 2, 3,),
                ints = intArrayOf( 0, 1, 2, 3, 4,),
                longs = longArrayOf( 1000, 2000,),
                floats = floatArrayOf( 1234.0f, 2.5f,),
                doubles = doubleArrayOf( 56789.0, 0.5,),
                booleans = booleanArrayOf( true, false,),
                chars = charArrayOf( 'A', 'B',),
            )
        """.trimIndent()

        val actual = withPrimitiveArrays.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun jdkCollectionTypes() {
        val expected = """
            WithJdkCollections(
                arrayList = arrayListOf<Int>( 0, 1, 2,),
                linkedSet = linkedSetOf<Int>( 0, 1, 2,),
                linkedMap = linkedMapOf<Int,String>(
                    1 to "a",
                    2 to "b",
                ),
                hashSet = hashSetOf<Int>( 7,),
                hashMap = hashMapOf<Int,String>(
                    7 to "seven",
                ),
            )
        """.trimIndent()

        val actual = withJdkCollections.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun nullablePropertiesWhenAbsent() {
        val expected = """
            WithNullables(
                name = null,
                count = null,
                items = null,
                someMap = null,
                ints = null,
                person = null,
            )
        """.trimIndent()

        val actual = withNullablesEmpty.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun nullablePropertiesWhenPresent() {
        val expected = """
            WithNullables(
                name = "Dave",
                count = 3,
                items = listOf<Int>( 0, 1,),
                someMap = mapOf<Int,String>(
                    1 to "a",
                ),
                ints = intArrayOf( 7, 8,),
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
            )
        """.trimIndent()

        val actual = withNullablesPopulated.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun mapWithCollectionValues() {
        val expected = """
            WithCollectionMapValues(
                listsByName = mapOf<String,List<Int>>(
                    "a" to listOf<Int>( 1, 2,),
                ),
                setsByName = mapOf<String,Set<String>>(
                    "b" to setOf<String>( "x", "y",),
                ),
            )
        """.trimIndent()

        val actual = withCollectionMapValues.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun enumsPrintQualified() {
        val expected = """
            WithEnums(
                direction = Direction.NORTH,
                maybeDirection = null,
                directions = listOf<Direction>( Direction.NORTH, Direction.SOUTH,),
                bySide = mapOf<Direction,String>(
                    Direction.NORTH to "up",
                ),
                toDirections = mapOf<String,List<Direction>>(
                    "all" to listOf<Direction>( Direction.NORTH, Direction.SOUTH,),
                ),
            )
        """.trimIndent()

        val actual = withEnums.deepPrint()
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
        val expected = """
            WithAnnotatedProperty(
                label = "some label",
                name = 
                    Name(
                        name = "some name",
                    ),
            )
        """.trimIndent()
        val actual = withAnnotatedProperty.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun mapStandalonePrimitives() {
        val expected = """
            mapOf(
                1 to "Hi",
                2 to "By",
                3 to "Aloha",
            )
        """.trimIndent()
        val actual = primitivesMap.deepPrint({it.deepPrint()}, {it.deepPrint()})
        assertEquals(expected, actual)
    }
    
    @Test
    fun withAMapPrimitives() {
        val expected = """
            WithAMap(
                id = 123,
                someMap = mapOf<Int,String>(
                    1 to "Hi",
                    2 to "By",
                    3 to "Aloha",
                ),
            )
        """.trimIndent()
        val actual = withAMap.deepPrint()
        assertEquals(expected, actual)
    }
    
    @Test
    fun withAMapWithDataClasses() {
        val expected = """
            WithMapDataClasses(
                id = 204,
                someMap = mapOf<Int,Surfer>(
                    1 to 
                        Surfer(
                            name = "Honolua Blomfield",
                            surfboard = 
                                Surfboard(
                                    length = 11.5f,
                                    width = 2.0f,
                                    style = "longboard",
                                ),
                        ),
                    2 to 
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
        val actual = withAMapDataClasses.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun withAMutableMapPrimitives() {
        val expected = """
            WithAMutableMap(
                id = 123,
                someMutableMap = mutableMapOf<Int,String>(
                    1 to "Hi",
                    2 to "By",
                    3 to "Aloha",
                ),
            )
        """.trimIndent()
        val actual = withAMutableMap.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun withAMutableMapWithDataClasses() {
        val expected = """
            WithMutableMapDataClasses(
                id = 204,
                someMutableMap = mutableMapOf<Int,Surfer>(
                    1 to 
                        Surfer(
                            name = "Honolua Blomfield",
                            surfboard = 
                                Surfboard(
                                    length = 11.5f,
                                    width = 2.0f,
                                    style = "longboard",
                                ),
                        ),
                    2 to 
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
        val actual = withAMutableMapDataClasses.deepPrint()
        assertEquals(expected, actual)
    }
}
