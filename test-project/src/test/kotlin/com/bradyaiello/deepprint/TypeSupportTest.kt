package com.bradyaiello.deepprint

import com.bradyaiello.deepprint.testclasses.WithASequence
import com.bradyaiello.deepprint.testclasses.deepPrint
import com.bradyaiello.deepprint.testobjects.usingUnannotatedDataClassFromExternalModule
import com.bradyaiello.deepprint.testobjects.withGenericAliases
import com.bradyaiello.deepprint.testobjects.withNestedCollections
import com.bradyaiello.deepprint.testobjects.withNestedMaps
import com.bradyaiello.deepprint.testobjects.withReadOnlyCollections
import com.bradyaiello.deepprint.testobjects.withTuples
import com.bradyaiello.deepprint.testobjects.withTypeAliasProperty
import com.bradyaiello.deepprint.testobjects.withTypedCollectionItems
import com.bradyaiello.deepprint.testobjects.withUnsigned
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Coverage for the property types DeepPrint knows how to reconstruct, beyond the
 * primitives and the everyday collections covered in [BasicTest].
 */
class TypeSupportTest {

    @Test
    fun pairAndTriple() {
        val expected = """
            WithTuples(
                pair = Pair("a", 1),
                triple = Triple(1, true, 'x'),
                pairOfClasses = Pair(
                    Surfer(
                        name = "Honolua Blomfield",
                        surfboard = 
                            Surfboard(
                                length = 11.5f,
                                width = 2.0f,
                                style = "longboard",
                            ),
                    ), Direction.NORTH),
            )
        """.trimIndent()

        val actual = withTuples.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun readOnlyCollectionTypes() {
        val expected = """
            WithReadOnlyCollections(
                collection = listOf<Int>( 1, 2,),
                iterable = listOf<String>( "a", "b",),
                aliased = listOf<Int>( 5, 6,),
            )
        """.trimIndent()

        val actual = withReadOnlyCollections.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun unsignedTypes() {
        val expected = """
            WithUnsigned(
                byte = 1u,
                short = 2u,
                int = 3u,
                long = 4u,
                ints = uintArrayOf( 5u, 6u,),
                longs = ulongArrayOf( 7u, 8u,),
            )
        """.trimIndent()

        val actual = withUnsigned.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun collectionItemsUseTheirStaticType() {
        // Kotlin/JS erases Float, Double and Char to numbers at runtime, so identifying
        // items by their runtime type printed 2.0f as 2 and 'a' as 97. The item type is
        // known at code generation time, so it is used instead.
        val expected = """
            WithTypedCollectionItems(
                floats = listOf<Float>( 1.0f, 2.5f,),
                doubles = listOf<Double>( 1.0, 2.5,),
                chars = listOf<Char>( 'a', 'b',),
                uints = listOf<UInt>( 3u, 4u,),
            )
        """.trimIndent()

        val actual = withTypedCollectionItems.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun typeAliasResolvesToTheAliasedType() {
        // PersonAlias is a typealias for the annotated SamplePersonClass. The alias is
        // resolved, so the property deep prints rather than falling back to toString().
        val expected = """
            WithTypeAliasProperty(
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

        val actual = withTypeAliasProperty.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun nestedCollections() {
        val expected = """
            WithNestedCollections(
                listOfLists = listOf<List<Int>>( listOf<Int>( 0, 1,), listOf<Int>( 2,),),
                setOfLists = setOf<List<Int>>( listOf<Int>( 1, 2,),),
                listOfArrays = listOf<IntArray>( intArrayOf( 1, 2,),),
                deep = listOf<List<List<Int>>>( listOf<List<Int>>( listOf<Int>( 1,),),),
            )
        """.trimIndent()

        val actual = withNestedCollections.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun nestedMaps() {
        val expected = """
            WithNestedMaps(
                mapOfMaps = mapOf<String,Map<String, Int>>(
                    "a" to mapOf<String,Int>(
                        "b" to 1,
                    ),
                ),
                listKeyed = mapOf<List<Int>,String>(
                    listOf<Int>( 1, 2,) to "x",
                ),
                listOfMaps = listOf<Map<String, Int>>( mapOf<String,Int>(
                        "a" to 1,
                    ),),
            )
        """.trimIndent()

        val actual = withNestedMaps.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun genericTypeAliasesResolve() {
        // Mapping<V> = Map<String, V> puts the parameter in the second position, and
        // Grid<T> = List<List<T>> nests it, so neither substitutes positionally.
        val expected = """
            WithGenericAliases(
                mapping = mapOf<String,Int>(
                    "a" to 1,
                ),
                grid = listOf<List<Int>>( listOf<Int>( 1, 2,),),
            )
        """.trimIndent()

        val actual = withGenericAliases.deepPrint()
        assertEquals(expected, actual)
    }

    @Test
    fun sequencePropertyIsNotConsumed() {
        // A Sequence prints with toString() rather than being reconstructed, precisely
        // so that printing cannot iterate it. constrainOnce() makes that observable:
        // if deepPrint() touched the sequence, toList() below would throw.
        val sequence = sequenceOf(1, 2, 3).constrainOnce()
        val actual = WithASequence(name = "s", sequence = sequence).deepPrint()

        assertTrue(actual.contains("name = \"s\","))
        assertEquals(listOf(1, 2, 3), sequence.toList())
    }

    @Test
    fun dataClassFromAnotherModuleDeepPrints() {
        // ExternalDataClass lives in :external-module and cannot carry @DeepPrint --
        // the annotation is SOURCE retention, so it is gone by the time the class is a
        // dependency. The extension for it is generated here instead.
        val expected = """
            UsingUnannotatedDataClassFromExternalModule(
                externalDataClass = 
                    ExternalDataClass(
                        name = "Bruce Wayne",
                        age = 42,
                        interests = listOf<String>( "true crime podcasts", "calisthenics", "tinkering", "puzzle solving",),
                    ),
                id = "985270457834522",
            )
        """.trimIndent()

        val actual = usingUnannotatedDataClassFromExternalModule.deepPrint()
        assertEquals(expected, actual)
    }
}
