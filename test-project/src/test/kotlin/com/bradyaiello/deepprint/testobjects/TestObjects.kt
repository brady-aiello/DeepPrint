package com.bradyaiello.deepprint.testobjects

import com.bradyaiello.deepprint.testclasses.AllTypes
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
import com.bradyaiello.deepprint.testclasses.WithAnArray
import com.bradyaiello.deepprint.testclasses.WithAnnotatedProperty
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableArray
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableList
import com.bradyaiello.deepprint.testclasses.WithDeepPrintableMutableList
import com.bradyaiello.deepprint.testclasses.WithMapDataClasses
import com.bradyaiello.deepprint.testclasses.WithMutableMapDataClasses
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
