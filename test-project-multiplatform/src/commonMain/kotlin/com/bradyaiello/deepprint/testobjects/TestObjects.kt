package com.bradyaiello.deepprint.testobjects

import com.bradyaiello.deepprint.testclasses.AllTypes
import com.bradyaiello.deepprint.testclasses.Direction
import com.bradyaiello.deepprint.testclasses.Name
import com.bradyaiello.deepprint.testclasses.SampleClass
import com.bradyaiello.deepprint.testclasses.SamplePersonClass
import com.bradyaiello.deepprint.testclasses.Surfer
import com.bradyaiello.deepprint.testclasses.ThreeClassesDeep
import com.bradyaiello.deepprint.testclasses.ThreeClassesDeep2
import com.bradyaiello.deepprint.testclasses.ThreeClassesDeep3
import com.bradyaiello.deepprint.testclasses.ThreeDimCoordinate
import com.bradyaiello.deepprint.testclasses.ThreeDimLine
import com.bradyaiello.deepprint.testclasses.Weather
import com.bradyaiello.deepprint.testclasses.WithAList
import com.bradyaiello.deepprint.testclasses.WithAMap
import com.bradyaiello.deepprint.testclasses.WithAMutableList
import com.bradyaiello.deepprint.testclasses.WithAMutableMap
import com.bradyaiello.deepprint.testclasses.WithAMutableSet
import com.bradyaiello.deepprint.testclasses.WithASet
import com.bradyaiello.deepprint.testclasses.WithAnArray
import com.bradyaiello.deepprint.testclasses.WithAnnotatedProperty
import com.bradyaiello.deepprint.testclasses.WithCollectionMapValues
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableArray
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableList
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableMutableList
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableMutableSet
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableSet
import com.bradyaiello.deepprint.testclasses.WithEnums
import com.bradyaiello.deepprint.testclasses.WithJdkCollections
import com.bradyaiello.deepprint.testclasses.WithMapDataClasses
import com.bradyaiello.deepprint.testclasses.WithMutableMapDataClasses
import com.bradyaiello.deepprint.testclasses.WithNestedCollections
import com.bradyaiello.deepprint.testclasses.WithNestedMaps
import com.bradyaiello.deepprint.testclasses.WithNullables
import com.bradyaiello.deepprint.testclasses.WithPrimitiveArrays
import com.bradyaiello.deepprint.testclasses.WithReadOnlyCollections
import com.bradyaiello.deepprint.testclasses.WithTuples
import com.bradyaiello.deepprint.testclasses.WithTypeAliasProperty
import com.bradyaiello.deepprint.testclasses.WithTypedCollectionItems
import com.bradyaiello.deepprint.testclasses.WithUnsigned
import com.bradyaiello.deepprint.testclasses.otherpackage.Surfboard
import com.bradyaiello.deepprint.testclasses.otherpackage.Temperature

val sample = SampleClass(0.5f, 2.6f, "A point")

val person = SamplePersonClass(name = "Dave", sampleClass = sample)

val allTypes = AllTypes()

val threeDimCoordinateA = ThreeDimCoordinate(
    x = 0.7f,
    y = 32.5f,
    z = 92.1f,
    label = "Point A"
)

val threeDimCoordinateB = ThreeDimCoordinate(
    x = 99.2f,
    y = 154.7f,
    z = 23.3f,
    label = "Point B"
)

val threeDimLine = ThreeDimLine(
    start = threeDimCoordinateA,
    end = threeDimCoordinateB,
    lineLabel = "A -> B"
)

val threeDeep = ThreeClassesDeep(age = 37, person = person)

val threeDeep2 = ThreeClassesDeep2(person = person, age = 37)

val threeDeep2Wide = ThreeClassesDeep3(
    person = person,
    age = 55,
    sampleClass = sample
)

val surfer = Surfer(
    name = "Honolua Blomfield",
    surfboard = Surfboard(
        11.5F,
        2F,
        "longboard"
    )
)

val surfer2 = Surfer(
    name = "Kelly Slater",
    surfboard = Surfboard(
        5.9f,
        1.8F,
        "shortboard"
    )
)

val weather = Weather(
    temperature = Temperature(
        fahrenheit = 76.0F
    )
)

val withAList = WithAList(
    name = "some list",
    items = listOf<Int>(0, 1, 2, 3, 4)
)

val withAMutableList = WithAMutableList(
    name = "some list",
    items = mutableListOf<Int>(0, 1, 2, 3, 4)
)

val withAnArray = WithAnArray(
    name = "some list",
    items = arrayOf<Int>(0, 1, 2, 3, 4)
)

val primitivesMap = mapOf(1 to "Hi", 2 to "By", 3 to "Aloha")
val primitivesMutableMap = mutableMapOf(1 to "Hi", 2 to "By", 3 to "Aloha")

val withAMap = WithAMap(123, primitivesMap)
val withAMutableMap = WithAMutableMap(123, primitivesMutableMap)

val classesMap = mapOf(1 to surfer, 2 to surfer2)
val classesMutableMap = mutableMapOf(1 to surfer, 2 to surfer2)

val withAMapDataClasses = WithMapDataClasses(
    id = 204,
    someMap = classesMap
)

val withAMutableMapDataClasses = WithMutableMapDataClasses(
    id = 204,
    someMutableMap = classesMutableMap
)

val withDeepPrintableList = WithDeepPrintableList("a name", listOf(surfer, surfer2))

val withDeepPrintableMutableList = WithDeepPrintableMutableList("a name", mutableListOf(surfer, surfer2))

val withDeepPrintableArray = WithDeepPrintableArray("a name", arrayOf(surfer, surfer2))

val withASet = WithASet(
    name = "some set",
    items = setOf<Int>(0, 1, 2, 3, 4)
)

val withAMutableSet = WithAMutableSet(
    name = "some set",
    items = mutableSetOf<Int>(0, 1, 2, 3, 4)
)

val withDeepPrintableSet = WithDeepPrintableSet("a name", setOf(surfer, surfer2))

val withDeepPrintableMutableSet = WithDeepPrintableMutableSet("a name", mutableSetOf(surfer, surfer2))

val withAnEmptyDeepPrintableSet = WithDeepPrintableSet("a name", emptySet())

val withPrimitiveArrays = WithPrimitiveArrays(
    bytes = byteArrayOf(-1, 0, 1),
    shorts = shortArrayOf(2, 3),
    ints = intArrayOf(0, 1, 2, 3, 4),
    longs = longArrayOf(1000L, 2000L),
    floats = floatArrayOf(1234f, 2.5f),
    doubles = doubleArrayOf(56789.0, 0.5),
    booleans = booleanArrayOf(true, false),
    chars = charArrayOf('A', 'B'),
)

val withJdkCollections = WithJdkCollections(
    arrayList = arrayListOf(0, 1, 2),
    linkedSet = linkedSetOf(0, 1, 2),
    linkedMap = linkedMapOf(1 to "a", 2 to "b"),
    // Single entry: HashSet and HashMap have no order guarantee across platforms.
    hashSet = hashSetOf(7),
    hashMap = hashMapOf(7 to "seven"),
)

val withTypeAliasProperty = WithTypeAliasProperty(person = person)

val withNullablesEmpty = WithNullables(
    name = null,
    count = null,
    items = null,
    someMap = null,
    ints = null,
    person = null,
)

val withNullablesPopulated = WithNullables(
    name = "Dave",
    count = 3,
    items = listOf(0, 1),
    someMap = mapOf(1 to "a"),
    ints = intArrayOf(7, 8),
    person = person,
)

val withCollectionMapValues = WithCollectionMapValues(
    listsByName = mapOf("a" to listOf(1, 2)),
    setsByName = mapOf("b" to setOf("x", "y")),
)

val withEnums = WithEnums(
    direction = Direction.NORTH,
    maybeDirection = null,
    directions = listOf(Direction.NORTH, Direction.SOUTH),
    bySide = mapOf(Direction.NORTH to "up"),
    toDirections = mapOf("all" to listOf(Direction.NORTH, Direction.SOUTH)),
)

val withTuples = WithTuples(
    pair = "a" to 1,
    triple = Triple(1, true, 'x'),
    pairOfClasses = surfer to Direction.NORTH,
)

val withReadOnlyCollections = WithReadOnlyCollections(
    collection = listOf(1, 2),
    iterable = listOf("a", "b"),
    sequence = sequenceOf(3, 4),
    aliased = listOf(5, 6),
)

val withUnsigned = WithUnsigned(
    byte = 1u,
    short = 2u,
    int = 3u,
    long = 4u,
    ints = uintArrayOf(5u, 6u),
    longs = ulongArrayOf(7u, 8u),
)

val withTypedCollectionItems = WithTypedCollectionItems(
    floats = listOf(1f, 2.5f),
    doubles = listOf(1.0, 2.5),
    chars = listOf('a', 'b'),
    uints = listOf(3u, 4u),
)

val withNestedCollections = WithNestedCollections(
    listOfLists = listOf(listOf(0, 1), listOf(2)),
    setOfLists = setOf(listOf(1, 2)),
    listOfArrays = listOf(intArrayOf(1, 2)),
    deep = listOf(listOf(listOf(1))),
)

val withNestedMaps = WithNestedMaps(
    mapOfMaps = mapOf("a" to mapOf("b" to 1)),
    listKeyed = mapOf(listOf(1, 2) to "x"),
    listOfMaps = listOf(mapOf("a" to 1)),
)

val withAnnotatedProperty = WithAnnotatedProperty(
    label = "some label",
    name = Name("some name")
)

/* TODO(finish this test when external data classes supported)
val usingUnannotatedDataClassFromExternalModule = UsingUnannotatedDataClassFromExternalModule(
    externalDataClass = ExternalDataClass(
        name = "Bruce Wayne",
        age = 42,
        interests = listOf("true crime podcasts", "calisthenics", "tinkering", "puzzle solving")
    ),
    id = "985270457834522"
)
*/
