package com.bradyaiello.deepprint.testobjects
import com.bradyaiello.deepprint.testclasses.*
import com.bradyaiello.deepprint.testclasses.otherpackage.Surfboard
import com.bradyaiello.deepprint.testclasses.otherpackage.Temperature
import com.module.external.ExternalDataClass
import kotlinx.datetime.Instant

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

val withDeepPrintableList = WithDeepPrintableList("a name", listOf(surfer, surfer2))

val withDeepPrintableMutableList = WithDeepPrintableMutableList("a name", mutableListOf(surfer, surfer2))

val withDeepPrintableArray = WithDeepPrintableArray("a name", arrayOf(surfer, surfer2))

val withAnnotatedProperty = WithAnnotatedProperty(
    label = "some label",
    name = Name("some name")
)

val usingUnannotatedDataClassFromExternalModule = UsingUnannotatedDataClassFromExternalModule(
    externalDataClass = ExternalDataClass(
        name = "Bruce Wayne",
        age = 42,
        interests = listOf("true crime podcasts", "calisthenics", "tinkering", "puzzle solving")
    ),
    id = "985270457834522"
)
