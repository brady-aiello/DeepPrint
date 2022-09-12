package com.bradyaiello.deepprint.testobjects
import com.bradyaiello.deepprint.testclasses.*
import com.bradyaiello.deepprint.testclasses.otherpackage.Surfboard
import com.bradyaiello.deepprint.testclasses.otherpackage.Temperature

val sample = SampleClass(0.5f, 2.6f, "A point")

val person = SamplePersonClass(name = "Dave", sampleClass = sample)

val allTypes = AllTypes()

val threeDeep = ThreeClassesDeep(age = 37, person = person)

val threeDeep2 = ThreeClassesDeep2(person = person, age = 37)

val threeDeep2Wide = ThreeClassesDeep3(
    person = person,
    age = 55,
    sampleClass = sample
)

val surfer = Surfer(
    name = "Brady Aiello",
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

val withDeepPrintableList = WithDeepPrintableList("a name", listOf(surfer, surfer2))
